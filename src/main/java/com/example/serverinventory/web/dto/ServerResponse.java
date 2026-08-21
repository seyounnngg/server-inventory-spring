package com.example.serverinventory.web.dto;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerStatus;

import java.time.LocalDateTime;

public record ServerResponse(
        Long id,
        String hostname,
        String ipAddress,
        Integer port,
        ServerStatus status,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ServerResponse from(Server server) {
        return new ServerResponse(
                server.getId(),
                server.getHostname(),
                server.getIpAddress(),
                server.getPort(),
                server.getStatus(),
                server.getOwner() != null ? server.getOwner().getName() : null,
                server.getCreatedAt(),
                server.getUpdatedAt()
        );
    }
}
