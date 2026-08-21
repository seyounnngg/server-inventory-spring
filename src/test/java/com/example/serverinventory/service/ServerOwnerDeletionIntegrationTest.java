package com.example.serverinventory.service;

import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.repository.ServerRepository;
import com.example.serverinventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Server.owner에 건 FK(ON DELETE SET NULL)가 실제로 스키마에 반영되는지,
 * 사용자를 삭제해도 서버 이력(row)이 유실되지 않는지를 실제 DB(H2)로 검증한다.
 */
@DataJpaTest
class ServerOwnerDeletionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void deletingOwnerShouldKeepServerRowWithNullOwner() {
        User owner = userRepository.saveAndFlush(new User("lab-owner", "encoded", "랩 담당자", Role.ADMIN));
        Server server = serverRepository.saveAndFlush(
                new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, owner));
        Long serverId = server.getId();
        Long ownerId = owner.getId();

        // 영속성 컨텍스트를 비워, Hibernate가 메모리 상의 Server.owner 참조로
        // 삭제를 막지 않고 DB 레벨의 ON DELETE SET NULL 제약이 실제로 동작하는지 확인한다.
        entityManager.clear();

        userRepository.deleteById(ownerId);
        entityManager.flush();
        entityManager.clear();

        Server reloaded = serverRepository.findById(serverId).orElseThrow();
        assertThat(reloaded.getOwner()).isNull();
        assertThat(reloaded.getHostname()).isEqualTo("lab-server-01");
    }
}
