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

/**
 * owner_id는 ON DELETE SET NULL로 연결한다: 사용자가 삭제돼도 서버 등록/점검 이력은
 * 유실시키지 않고 "등록자 없음" 상태로 보존하기 위함 (CASCADE는 이력 유실 위험, RESTRICT는
 * 사용자 삭제 자체를 막아 관리 유연성이 떨어짐).
 */
@Entity
public class Server extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String hostname;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServerStatus status;

    @Column(name = "spec_sheet_path")
    private String specSheetPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(
            foreignKeyDefinition = "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL"))
    private User owner;

    protected Server() {
    }

    public Server(String hostname, String ipAddress, Integer port, ServerStatus status, User owner) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.status = status;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public String getSpecSheetPath() {
        return specSheetPath;
    }

    public void setSpecSheetPath(String specSheetPath) {
        this.specSheetPath = specSheetPath;
    }

    public User getOwner() {
        return owner;
    }
}
