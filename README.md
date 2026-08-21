# server-inventory-spring

> Spring Boot 공식 Getting Started Guides 1~10단계 학습 내용을 통합 적용한 개인 프로젝트.
> 1차 미니 CRUD 프로젝트([server-inventory](../server-inventory))에서 받은 교수 피드백 6가지를 전부 반영해 재설계했습니다.

## 1차 프로젝트 피드백 반영 내역

| # | 피드백 | 해결 방법 |
|---|---|---|
| 1 | 생성일자/수정일자 공통 필드 구조 부재 | `BaseEntity`(`@MappedSuperclass`)로 `createdAt`/`updatedAt`/`deletedAt`을 공통화하고 Spring Data JPA Auditing 적용 |
| 2 | 서버 포트번호 필드 누락 | `Server.port` 추가 (`@Min(1) @Max(65535)`, 선택 입력) |
| 3 | 삭제 정책 미흡 (CASCADE로 이력 유실 위험) | 소프트 삭제(`deletedAt`) 도입. 목록/상세 조회는 `deletedAt is null` 조건으로만 노출 |
| 4 | FK 설계가 데이터 성격을 고려하지 않음 | `Server.owner` FK를 `ON DELETE SET NULL`로 설계 (근거는 아래 3번 참고) |
| 5 | 비밀번호 평문 저장 | `BCryptPasswordEncoder`로 해싱 후 저장 |
| 6 | Git 브랜치 전략 부재 | `main` + 기능 단위 커밋으로 관리 (다음 확장 시 `feature/*` 브랜치 적용 예정) |

## 기술 스택

- Java 21 / Spring Boot 4.1.0 / Gradle
- Spring MVC + Thymeleaf (화면), Spring Data JPA (H2/MariaDB), Spring HATEOAS (REST API), Spring Security + BCrypt
- 테스트: JUnit 5, MockitoBean, RestTestClient, `@DataJpaTest`

## 프로젝트 구조

    server-inventory-spring
    ├── build.gradle / settings.gradle
    ├── docker-compose.yml           # 운영 가정용 MariaDB
    └── src
        ├── main
        │   ├── java/com/example/serverinventory
        │   │   ├── ServerInventoryApplication.java
        │   │   ├── config      # Security, JPA Auditing, MVC, 초기 admin 시드
        │   │   ├── domain      # BaseEntity, User, Server, Role, ServerStatus
        │   │   ├── repository  # Spring Data JPA
        │   │   ├── service     # UserService(UserDetailsService), ServerService, 파일 저장소
        │   │   ├── web         # Thymeleaf 컨트롤러 + REST(HATEOAS) 컨트롤러 + DTO
        │   │   └── exception   # 도메인 예외 + API/View 별 @*ControllerAdvice
        │   └── resources
        │       ├── application.properties          # 기본: H2 인메모리
        │       ├── application-mariadb.properties   # 운영 가정 프로필
        │       └── templates                        # login/signup/servers/error
        └── test
            └── java/com/example/serverinventory
                ├── ServerInventoryApplicationTests.java   # 스모크 테스트
                ├── web/ServerApiControllerTest.java        # @WebMvcTest + RestTestClient
                ├── web/ServerViewControllerSecurityTest.java # 역할별 접근 제어(403/200)
                ├── service/ServerServiceTest.java          # 소프트 삭제 단위 테스트
                └── service/ServerOwnerDeletionIntegrationTest.java # FK SET NULL 실제 DB 검증

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
서버 row가 남고 `owner`만 null이 되는 것을 실제 H2 DB로 검증한다.

## 실행 방법

### 1. 개발 모드 (H2 인메모리, 기본값)

    ./gradlew bootRun

최초 기동 시 로그인용 초기 계정이 자동 생성된다: `admin` / `admin1234!` (콘솔 로그에도 출력됨).
H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:serverinventory`, 계정 `sa`)

### 2. 운영 가정 모드 (MariaDB, Docker)

    docker compose up -d
    ./gradlew bootRun --args='--spring.profiles.active=mariadb'

## 권한 설계

| 기능 | VIEWER | ADMIN |
|---|:---:|:---:|
| 서버 목록/상세 조회 | ✅ | ✅ |
| 서버 등록/수정/삭제(소프트) | ❌ | ✅ |
| 스펙시트 업로드 | ❌ | ✅ |

회원가입으로 만든 계정은 기본적으로 `VIEWER` 권한을 가진다. `ADMIN` 계정은 초기 시드 계정을 사용하거나
DB에서 직접 `role` 값을 변경한다.

## API (HATEOAS)

- `GET /api/servers` — 서버 목록 (소프트 삭제된 서버는 제외)
- `GET /api/servers/{id}` — 서버 상세, 404 시 JSON 에러 바디 반환

응답에는 `self`/`servers` 링크가 항상 포함되고, 요청자가 `ADMIN` 권한을 가진 경우에만 `edit` 링크가 추가된다
(권한별 응답 차별화).

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

## 테스트

    ./gradlew test

- `ServerInventoryApplicationTests`: 전체 빈 등록 확인 (스모크 테스트)
- `ServerApiControllerTest`: `@WebMvcTest` + `RestTestClient`로 인증/미인증 응답, 404 응답 검증
- `ServerViewControllerSecurityTest`: VIEWER의 등록 폼 접근 시 403, ADMIN은 200 확인
- `ServerServiceTest`: 소프트 삭제 시 row가 지워지지 않고 `deletedAt`만 채워지는지 단위 테스트로 검증
- `ServerOwnerDeletionIntegrationTest`: `@DataJpaTest`로 실제 H2에 FK(`ON DELETE SET NULL`)를 반영해
  사용자 삭제 시 서버 row는 남고 `owner`만 null이 되는지 검증

## 향후 확장 (제출 범위 밖)

- 실제 랩 서버 정보로 더미 데이터 교체
- 서버별 점검 이력(Audit Log) 테이블 분리 (1차 프로젝트의 `server_check` 개념 재도입)
- `ServerStatus`를 실제 핑/헬스체크와 연동
