# server-inventory-spring

> Spring Boot 공식 Getting Started Guides 1~10단계 학습 내용을 통합 적용한 개인 프로젝트.
> 1차 미니 CRUD 프로젝트([server-inventory](../server-inventory))에서 받은 교수 피드백 6가지를 전부 반영해 재설계했습니다.

## 1차 프로젝트 피드백 반영 내역

| # | 피드백 | 해결 방법 |
|---|---|---|
| 1 | 생성일자/수정일자 공통 필드 구조 부재 | `BaseEntity`(`@MappedSuperclass`)로 `createdAt`/`updatedAt`/`deletedAt`을 공통화하고 Spring Data JPA Auditing 적용 |
| 2 | 서버 포트번호 필드 누락 | `Server.port` 추가 (`@Min(1) @Max(65535)`, 선택 입력) |
| 3 | 삭제 정책 미흡 (CASCADE로 이력 유실 위험) | 소프트 삭제(`deletedAt`) 도입. 목록/상세 조회는 `deletedAt is null` 조건으로만 노출 |
| 4 | FK 설계가 데이터 성격을 고려하지 않음 | `Server.owner` FK를 `ON DELETE SET NULL`로 설계 (근거는 아래 참고) |
| 5 | 비밀번호 평문 저장 | `BCryptPasswordEncoder`로 해싱 후 저장 |
| 6 | Git 브랜치 전략 부재 | 기능 단위로 `feature/*` 브랜치를 만들어 작업 후 `main`에 `--no-ff` 머지 (아래 "Git 브랜치 전략" 참고) |

## 기술 스택

- Java 21 / Spring Boot 4.1.0 / Gradle
- Spring MVC + Thymeleaf (화면, Bootstrap 5 적용), Spring Data JPA (H2/MariaDB), Spring HATEOAS (REST API),
  Spring Security + BCrypt, `@Scheduled` 기반 상시 모니터링
- 테스트: JUnit 5, MockitoBean, RestTestClient, `@DataJpaTest`

## 프로젝트 구조

    server-inventory-spring
    ├── build.gradle / settings.gradle
    ├── docker-compose.yml           # 운영 가정용 MariaDB
    └── src
        ├── main
        │   ├── java/com/example/serverinventory
        │   │   ├── ServerInventoryApplication.java
        │   │   ├── config      # Security, JPA Auditing, MVC, PingScheduler, 초기 admin 시드
        │   │   ├── domain      # BaseEntity, User, Server, ServerCheck, ServerAuditLog, Role, ServerStatus, CheckResult
        │   │   ├── repository  # Spring Data JPA
        │   │   ├── service     # UserService(UserDetailsService), ServerService, ServerCheckService,
        │   │   │                 ServerAuditLogService, PingClient, 파일 저장소
        │   │   ├── web         # Thymeleaf 컨트롤러 + REST(HATEOAS) 컨트롤러 + DTO
        │   │   └── exception   # 도메인 예외 + API/View 별 @*ControllerAdvice
        │   └── resources
        │       ├── application.properties          # 기본: H2 인메모리 + 핑 스케줄러 설정
        │       ├── application-mariadb.properties   # 운영 가정 프로필
        │       └── templates                        # login/signup/servers/admin/error (+ fragments/nav)
        └── test
            └── java/com/example/serverinventory      # 37개 테스트 (아래 "테스트" 참고)

## 도메인 모델

### `BaseEntity` (공통 감사 필드)

`@CreatedDate`/`@LastModifiedDate`(Spring Data JPA Auditing)로 생성/수정 시각을 자동 기록하고,
`deletedAt`이 null이 아니면 삭제된 것으로 간주하는 소프트 삭제 필드를 둔다.

### `Server.owner` FK를 `ON DELETE SET NULL`로 설계한 이유

담당자(`User`)가 삭제될 때 그 사용자가 등록한 서버 기록까지 같이 사라지면 점검 이력이 통째로 유실될 위험이 있다.

- `CASCADE`: 사용자 삭제 시 서버 기록까지 삭제 → 이력 유실 위험
- `RESTRICT`(기본값): 등록한 서버가 하나라도 있으면 사용자 삭제 자체가 불가능 → 관리 유연성 저하
- **`SET NULL` 채택**: 사용자가 삭제돼도 서버 기록은 "등록자 없음" 상태로 유지 → 이력 보존과 삭제 유연성을 동시에 확보

`Server` 엔티티의 `@JoinColumn(foreignKey = @ForeignKey(foreignKeyDefinition = "... ON DELETE SET NULL"))`로
스키마에 직접 반영했고, `ServerOwnerDeletionIntegrationTest`(`@DataJpaTest`)에서 사용자를 삭제해도
서버 row가 남고 `owner`만 null이 되는 것을 실제 DB로 검증한다. **H2뿐 아니라 실제 MariaDB(Docker)로도
직접 기동해 `information_schema.REFERENTIAL_CONSTRAINTS`에서 `owner_id → SET NULL`,
`server_check.server_id → CASCADE`가 그대로 반영되는 것을 확인했다.**

### `ServerCheck` / `ServerAuditLog` — 점검 이력과 변경 이력을 분리한 이유

- `ServerCheck`(핑 UP/DOWN 결과)는 `server` FK에 `ON DELETE CASCADE`를 건다. 점검 이력은 그 서버가
  존재할 때만 의미가 있는 종속 데이터이기 때문이다.
- `ServerAuditLog`(등록/수정/삭제 diff)는 반대로 `Server` row와 FK를 걸지 않고 `serverId`를 평범한
  값으로만 들고 있다. 감사 로그는 "그때 무슨 일이 있었는지"를 원본 대상이 사라지거나 바뀌어도 그대로
  보존해야 하는 append-only 기록이라는 성격이 다르기 때문이다.

## 실행 방법

### 1. 개발 모드 (H2 인메모리, 기본값)

    ./gradlew bootRun

최초 기동 시 로그인용 초기 계정이 자동 생성된다: `admin` / `admin1234!` (콘솔 로그에도 출력됨).
H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:serverinventory`, 계정 `sa`)

### 2. 운영 가정 모드 (MariaDB, Docker)

    docker compose up -d
    ./gradlew bootRun --args='--spring.profiles.active=mariadb'

`docker-compose.yml`은 호스트 포트 `3306`을 그대로 사용한다. 로컬에 이미 다른 MariaDB/MySQL이
`3306`을 점유하고 있다면(`lsof -i :3306`으로 확인) `docker-compose.yml`의 `ports`를 예: `"3307:3306"`으로
바꾸고, `application-mariadb.properties`의 `spring.datasource.url` 포트도 함께 맞춰야 한다.

## 권한 설계

| 기능 | VIEWER | ADMIN |
|---|:---:|:---:|
| 서버 목록/상세 조회, DOWN 서버 조회 | ✅ | ✅ |
| 서버 등록/수정/삭제(소프트) | ❌ | ✅ |
| 스펙시트 업로드 | ❌ | ✅ |
| 수동 점검("지금 점검" / "전체 서버 일괄 점검") | ❌ | ✅ |
| 사용자 권한 관리(`/admin/users`) | ❌ | ✅ |

회원가입으로 만든 계정은 기본적으로 `VIEWER` 권한을 가진다. `ADMIN`은 초기 시드 계정(`admin`)으로
로그인한 뒤 `/admin/users`에서 다른 사용자의 권한을 `ADMIN`으로 올려줄 수 있다 (단, 자기 자신의
권한은 셀프 락아웃 방지를 위해 변경할 수 없다).

## 상시 핑 점검 (스케줄러 + 수동 트리거)

방치된 서버를 사람이 잊고 점검하지 않는 상황을 막기 위해 `@Scheduled`로 전체 서버를 주기적으로
자동 점검한다 (기본 5분 간격, `monitoring.ping.*` 프로퍼티로 조정/비활성화 가능). 동시에 배포 직후처럼
즉시 확인이 필요한 경우를 위해 화면에 "지금 점검"(서버 1대)과 "전체 서버 일괄 점검" 버튼도 남겨뒀다.
점검 결과(UP/DOWN)는 `ServerCheck`로 이력이 쌓이고, `/servers/down`에서 최신 점검이 DOWN인 서버만
모아볼 수 있다. 점검 자체가 `Server.status`(RUNNING/STOPPED/...)를 자동으로 바꾸지는 않는다 —
운영 판단은 관리자가 하도록 관측 값과 상태 값을 분리했다.

## API (HATEOAS)

- `GET /api/servers` — 서버 목록 (페이지네이션, 소프트 삭제 제외)
- `GET /api/servers/{id}` — 서버 상세, 404 시 JSON 에러 바디 반환
- `POST /api/servers` — 서버 등록 (ADMIN, 201 + `Location` 헤더)
- `PUT /api/servers/{id}` — 서버 수정 (ADMIN)
- `DELETE /api/servers/{id}` — 서버 소프트 삭제 (ADMIN, 204)

응답에는 `self`/`servers` 링크가 항상 포함되고, 요청자가 `ADMIN` 권한을 가진 경우에만 `edit` 링크가 추가된다
(권한별 응답 차별화). `/api/**`는 JSON 바디를 요구해 단순 HTML 폼으로는 트리거될 수 없으므로 CSRF 예외로
두었다 (화면(`/servers/**`)은 세션 쿠키 + 폼 제출 방식 그대로라 CSRF 토큰을 계속 요구한다).

```json
{
  "id": 1,
  "hostname": "lab-server-01",
  "ipAddress": "192.168.0.10",
  "port": 22,
  "status": "RUNNING",
  "ownerName": "관리자",
  "_links": {
    "self": { "href": "http://localhost:8080/api/servers/1" },
    "servers": { "href": "http://localhost:8080/api/servers" },
    "edit": { "href": "/servers/1/edit" }
  }
}
```

목록 조회는 `PagedModel`로 응답하며 `page` 메타데이터와 `prev`/`next` 링크가 함께 내려간다:
`GET /api/servers?page=0&size=10`.

## 테스트

    ./gradlew test

37개 테스트가 있으며 크게 네 갈래로 나뉜다.

- **스모크**: `ServerInventoryApplicationTests` — 전체 빈 등록 확인
- **웹 계층(`@WebMvcTest` + `RestTestClient`)**: `ServerApiControllerTest`(CRUD·404·권한 403),
  `ServerViewControllerSecurityTest`(VIEWER/ADMIN 접근 제어)
- **서비스 단위 테스트(Mockito)**: `ServerServiceTest`(소프트 삭제·변경 이력 기록),
  `ServerCheckServiceTest`(UP/DOWN 판정), `UserServiceTest`(회원가입 중복·비밀번호 해싱·권한 변경),
  `FileSystemStorageServiceTest`(파일 저장/조회), `ServerFormValidationTest`(IPv4·포트 범위 Bean Validation),
  `AuthControllerTest`(회원가입 성공/중복/검증실패)
- **DB 통합 테스트(`@DataJpaTest`)**: `ServerOwnerDeletionIntegrationTest`(FK `ON DELETE SET NULL` 검증),
  `ServerCheckRepositoryTest`(fetch join으로 지연 로딩 예외 없이 조회되는지 검증 — 아래 회고 참고)

## Git 브랜치 전략

기능 단위로 브랜치를 나눠 작업하고 `main`에 `--no-ff`로 머지했다 (커밋 이력에 각 브랜치가 그대로 남는다).

    feature/server-history   핑 점검 스케줄러 + 변경 이력(감사 로그)
    feature/rest-api-crud    REST API 서버 등록/수정/삭제(HATEOAS)
    feature/user-management  관리자용 사용자 권한 관리
    feature/pagination       서버 목록 페이지네이션(화면+API)
    feature/test-coverage    회원가입 중복/파일 업로드/폼 검증 테스트 보강
    feature/bootstrap-ui     Bootstrap 5 적용 + 발견된 버그 수정

## 개발 중 발견하고 고친 버그

- **API 목록 조회 500 (LazyInitializationException)**: `ServerRepository.findAllByDeletedAtIsNull()`이
  `owner`를 fetch join하지 않아, 트랜잭션이 끝난 뒤 컨트롤러에서 `server.getOwner()`에 접근할 때 터졌다.
  JPQL에 `left join fetch s.owner`를 추가해 해결.
- **`/servers/down` 500 (LazyInitializationException)**: 같은 종류의 문제가
  `ServerCheckRepository.findLatestPerServer()`에도 있었다 — `ServerCheck.server`, `Server.owner`가
  모두 지연 로딩 상태로 뷰 렌더링 시점에 세션이 이미 닫혀 있었다. 쿼리에 `join fetch c.server s
  left join fetch s.owner`를 추가하고, 회귀 방지용 `ServerCheckRepositoryTest`를 추가했다.
- **HATEOAS 페이지 링크가 `{id}`를 문자 그대로 담고 있던 문제**: `linkTo(methodOn(...).detail(server.getId()))`를
  `.toUri()`로 바로 변환하면, 테스트처럼 id가 아직 없는 상태(mock)에서 `Illegal character in path` 예외가 났다.
  또한 `Pageable`을 인자로 받는 메서드에 대해서는 이 환경에서 `methodOn` 기반 링크 빌더가 `page`/`size`
  쿼리 파라미터를 직렬화하지 못해, `self`/`next` 링크에 파라미터가 누락되는 문제가 있었다. 페이지 링크는
  `ServletUriComponentsBuilder`로 현재 요청 URI를 기준으로 직접 조립하도록 바꿔 해결했다.

## 향후 확장 (제출 범위 밖)

- 실제 랩 서버 정보로 더미 데이터 교체
- `ServerStatus`를 점검 결과와 연동해 자동 전이할지 여부 검토 (현재는 의도적으로 분리)
- CI(GitHub Actions)로 `./gradlew test` 자동화
