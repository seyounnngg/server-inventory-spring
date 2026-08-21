package com.example.serverinventory.repository;

import com.example.serverinventory.domain.ServerCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServerCheckRepository extends JpaRepository<ServerCheck, Long> {

    List<ServerCheck> findByServerIdOrderByCheckedAtDesc(Long serverId);

    @Query("select c from ServerCheck c where c.server.id = :serverId order by c.checkedAt desc limit 1")
    Optional<ServerCheck> findLatestByServerId(Long serverId);

    @Query("""
            select c from ServerCheck c
            where c.checkedAt = (
                select max(c2.checkedAt) from ServerCheck c2 where c2.server.id = c.server.id
            )
            """)
    List<ServerCheck> findLatestPerServer();
}
