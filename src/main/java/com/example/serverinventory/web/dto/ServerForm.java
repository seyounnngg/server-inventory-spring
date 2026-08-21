package com.example.serverinventory.web.dto;

import com.example.serverinventory.domain.ServerStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ServerForm {

    @NotBlank(message = "호스트명은 필수입니다.")
    private String hostname;

    @NotBlank(message = "IP 주소는 필수입니다.")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$",
            message = "올바른 IPv4 형식이 아닙니다."
    )
    private String ipAddress;

    @Min(value = 1, message = "포트는 1 이상이어야 합니다.")
    @Max(value = 65535, message = "포트는 65535 이하이어야 합니다.")
    private Integer port;

    @NotNull(message = "상태를 선택해주세요.")
    private ServerStatus status;

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
}
