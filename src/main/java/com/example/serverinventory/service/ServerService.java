package com.example.serverinventory.service;

import com.example.serverinventory.domain.AuditAction;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.repository.ServerRepository;
import com.example.serverinventory.service.storage.StorageService;
import com.example.serverinventory.web.dto.ServerForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ServerService {

    private final ServerRepository serverRepository;
    private final StorageService storageService;
    private final ServerAuditLogService auditLogService;

    public ServerService(ServerRepository serverRepository, StorageService storageService,
                          ServerAuditLogService auditLogService) {
        this.serverRepository = serverRepository;
        this.storageService = storageService;
        this.auditLogService = auditLogService;
    }

    public List<Server> listActive() {
        return serverRepository.findAllByDeletedAtIsNull();
    }

    public Page<Server> listActive(Pageable pageable) {
        return serverRepository.findAllByDeletedAtIsNull(pageable);
    }

    public Server get(Long id) {
        return serverRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ServerNotFoundException(id));
    }

    @Transactional
    public Server create(ServerForm form, User owner) {
        Server server = new Server(form.getHostname(), form.getIpAddress(), form.getPort(), form.getStatus(), owner);
        server = serverRepository.save(server);
        auditLogService.record(server.getId(), AuditAction.CREATE,
                "등록: hostname=%s, ip=%s, port=%s, status=%s".formatted(
                        server.getHostname(), server.getIpAddress(), server.getPort(), server.getStatus()));
        return server;
    }

    @Transactional
    public Server update(Long id, ServerForm form) {
        Server server = get(id);

        List<String> changes = new ArrayList<>();
        diff(changes, "hostname", server.getHostname(), form.getHostname());
        diff(changes, "ipAddress", server.getIpAddress(), form.getIpAddress());
        diff(changes, "port", server.getPort(), form.getPort());
        diff(changes, "status", server.getStatus(), form.getStatus());

        server.setHostname(form.getHostname());
        server.setIpAddress(form.getIpAddress());
        server.setPort(form.getPort());
        server.setStatus(form.getStatus());

        if (!changes.isEmpty()) {
            auditLogService.record(id, AuditAction.UPDATE, String.join(", ", changes));
        }
        return server;
    }

    @Transactional
    public void softDelete(Long id) {
        Server server = get(id);
        server.markDeleted();
        auditLogService.record(id, AuditAction.DELETE, "삭제(소프트): hostname=" + server.getHostname());
    }

    @Transactional
    public void attachSpecSheet(Long id, MultipartFile file) {
        Server server = get(id);
        String storedFilename = storageService.store(file);
        server.setSpecSheetPath(storedFilename);
    }

    private void diff(List<String> changes, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add("%s: %s -> %s".formatted(field, before, after));
        }
    }
}
