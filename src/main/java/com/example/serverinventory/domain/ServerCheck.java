package com.example.serverinventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * server_id FK는 CASCADE로 연결한다: 점검 이력은 그 서버가 존재할 때만 의미가 있고,
 * Server는 평소 소프트 삭제만 되므로 하드 삭제(관리자 DB 정리 등) 시에만 함께 정리되면 된다.
 */
@Entity
public class ServerCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false, foreignKey = @ForeignKey(
            foreignKeyDefinition = "FOREIGN KEY (server_id) REFERENCES server(id) ON DELETE CASCADE"))
    private Server server;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CheckResult result;

    protected ServerCheck() {
    }

    public ServerCheck(Server server, LocalDateTime checkedAt, CheckResult result) {
        this.server = server;
        this.checkedAt = checkedAt;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public Server getServer() {
        return server;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public CheckResult getResult() {
        return result;
    }
}
