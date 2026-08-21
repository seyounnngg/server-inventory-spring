package com.example.serverinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/**
 * 서버 자체(Server row)와는 의도적으로 FK를 걸지 않는다: 감사 로그는 대상이 사라지거나
 * 바뀌어도 "그때 무슨 일이 있었는지"를 그대로 보존해야 하는 append-only 기록이기 때문이다.
 */
@Entity
public class ServerAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    @Column(nullable = false, length = 50)
    private String actorUsername;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 500)
    private String detail;

    protected ServerAuditLog() {
    }

    public ServerAuditLog(Long serverId, AuditAction action, String actorUsername, String detail) {
        this.serverId = serverId;
        this.action = action;
        this.actorUsername = actorUsername;
        this.changedAt = LocalDateTime.now();
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public Long getServerId() {
        return serverId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getDetail() {
        return detail;
    }
}
