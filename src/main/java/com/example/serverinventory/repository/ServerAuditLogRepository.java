package com.example.serverinventory.repository;

import com.example.serverinventory.domain.ServerAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerAuditLogRepository extends JpaRepository<ServerAuditLog, Long> {

    List<ServerAuditLog> findByServerIdOrderByChangedAtDesc(Long serverId);
}
