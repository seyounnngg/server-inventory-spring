package com.example.serverinventory.config;

import com.example.serverinventory.service.ServerCheckService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 방치된 서버를 사람이 잊고 점검하지 않는 상황을 막기 위해 주기적으로 전체 서버를 자동 점검한다.
 * 배포 직후처럼 즉시 확인이 필요한 경우를 위한 수동 트리거(ServerViewController)는 별도로 남겨둔다.
 */
@Configuration
@EnableScheduling
public class PingScheduler {

    @ConfigurationProperties("monitoring.ping")
    public record PingProperties(@DefaultValue("true") boolean enabled) {
    }

    private final ServerCheckService serverCheckService;
    private final PingProperties properties;

    public PingScheduler(ServerCheckService serverCheckService, PingProperties properties) {
        this.serverCheckService = serverCheckService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${monitoring.ping.fixed-delay-ms:300000}",
            initialDelayString = "${monitoring.ping.initial-delay-ms:30000}")
    public void checkAllServers() {
        if (!properties.enabled()) {
            return;
        }
        serverCheckService.checkAllActive();
    }
}
