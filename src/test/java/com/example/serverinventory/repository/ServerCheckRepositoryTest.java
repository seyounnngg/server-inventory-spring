package com.example.serverinventory.repository;

import com.example.serverinventory.domain.CheckResult;
import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerCheck;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * findLatestPerServer()가 server/owner를 fetch join으로 미리 채워두는지 검증한다.
 * (fetch join이 빠지면 영속성 컨텍스트가 닫힌 뒤 화면 렌더링에서 LazyInitializationException이 난다 -
 * /servers/down 페이지에서 실제로 겪었던 회귀 버그)
 */
@DataJpaTest
class ServerCheckRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerCheckRepository serverCheckRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findLatestPerServerShouldEagerlyLoadServerAndOwner() {
        User owner = userRepository.save(new User("lab-owner", "encoded", "랩 담당자", Role.ADMIN));
        Server server = serverRepository.save(
                new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, owner));
        serverCheckRepository.save(new ServerCheck(server, LocalDateTime.now(), CheckResult.DOWN));
        entityManager.flush();
        entityManager.clear();

        List<ServerCheck> latest = serverCheckRepository.findLatestPerServer();
        entityManager.clear();

        assertThat(latest).hasSize(1);
        assertThatCode(() -> {
            String hostname = latest.get(0).getServer().getHostname();
            String ownerName = latest.get(0).getServer().getOwner().getName();
            assertThat(hostname).isEqualTo("lab-server-01");
            assertThat(ownerName).isEqualTo("랩 담당자");
        }).doesNotThrowAnyException();
    }
}
