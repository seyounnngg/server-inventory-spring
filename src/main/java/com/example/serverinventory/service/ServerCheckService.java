package com.example.serverinventory.service;

import com.example.serverinventory.domain.CheckResult;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerCheck;
import com.example.serverinventory.repository.ServerCheckRepository;
import com.example.serverinventory.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ServerCheckService {

    private static final int PING_TIMEOUT_MILLIS = 2000;

    private static final Logger log = LoggerFactory.getLogger(ServerCheckService.class);

    private final ServerRepository serverRepository;
    private final ServerCheckRepository serverCheckRepository;
    private final PingClient pingClient;

    public ServerCheckService(ServerRepository serverRepository, ServerCheckRepository serverCheckRepository,
                               PingClient pingClient) {
        this.serverRepository = serverRepository;
        this.serverCheckRepository = serverCheckRepository;
        this.pingClient = pingClient;
    }

    @Transactional
    public ServerCheck checkOne(Long serverId) {
        Server server = serverRepository.findByIdAndDeletedAtIsNull(serverId)
                .orElseThrow(() -> new com.example.serverinventory.exception.ServerNotFoundException(serverId));
        return ping(server);
    }

    @Transactional
    public List<ServerCheck> checkAllActive() {
        List<Server> servers = serverRepository.findAllByDeletedAtIsNull();
        log.info("전체 서버 일괄 점검 시작: {}대", servers.size());
        return servers.stream().map(this::ping).toList();
    }

    private ServerCheck ping(Server server) {
        boolean reachable = pingClient.isReachable(server.getIpAddress(), PING_TIMEOUT_MILLIS);
        ServerCheck check = new ServerCheck(server, LocalDateTime.now(), reachable ? CheckResult.UP : CheckResult.DOWN);
        return serverCheckRepository.save(check);
    }

    public List<ServerCheck> history(Long serverId) {
        return serverCheckRepository.findByServerIdOrderByCheckedAtDesc(serverId);
    }

    /**
     * 서버별 "마지막 점검" 1건씩만 모은 맵. 담당자 대시보드에서 DOWN 상태만 골라내는 데 쓴다.
     */
    public Map<Long, ServerCheck> latestChecksByServerId() {
        return serverCheckRepository.findLatestPerServer().stream()
                .collect(Collectors.toMap(c -> c.getServer().getId(), Function.identity()));
    }
}
