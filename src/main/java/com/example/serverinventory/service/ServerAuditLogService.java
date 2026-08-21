package com.example.serverinventory.service;

import com.example.serverinventory.domain.AuditAction;
import com.example.serverinventory.domain.ServerAuditLog;
import com.example.serverinventory.repository.ServerAuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServerAuditLogService {

    private final ServerAuditLogRepository repository;

    public ServerAuditLogService(ServerAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(Long serverId, AuditAction action, String detail) {
        repository.save(new ServerAuditLog(serverId, action, currentUsername(), detail));
    }

    public List<ServerAuditLog> history(Long serverId) {
        return repository.findByServerIdOrderByChangedAtDesc(serverId);
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
