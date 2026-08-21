package com.example.serverinventory.service;

import com.example.serverinventory.domain.CheckResult;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerCheck;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.repository.ServerCheckRepository;
import com.example.serverinventory.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerCheckServiceTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private ServerCheckRepository serverCheckRepository;

    @Mock
    private PingClient pingClient;

    @Test
    void checkOneShouldRecordDownWhenHostUnreachable() {
        ServerCheckService service = new ServerCheckService(serverRepository, serverCheckRepository, pingClient);
        Server server = new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, null);
        when(serverRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(server));
        when(pingClient.isReachable("192.168.0.10", 2000)).thenReturn(false);
        when(serverCheckRepository.save(any(ServerCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServerCheck result = service.checkOne(1L);

        assertThat(result.getResult()).isEqualTo(CheckResult.DOWN);
    }

    @Test
    void checkAllActiveShouldCheckEveryNonDeletedServer() {
        ServerCheckService service = new ServerCheckService(serverRepository, serverCheckRepository, pingClient);
        Server server1 = new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, null);
        Server server2 = new Server("lab-server-02", "192.168.0.11", 22, ServerStatus.RUNNING, null);
        when(serverRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(server1, server2));
        when(pingClient.isReachable(any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(true);
        when(serverCheckRepository.save(any(ServerCheck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ServerCheck> results = service.checkAllActive();

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(c -> c.getResult() == CheckResult.UP);
    }
}
