package com.example.serverinventory.web;

import com.example.serverinventory.config.WebSecurityConfig;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.service.ServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ServerApiController.class)
@AutoConfigureRestTestClient
@Import(WebSecurityConfig.class)
class ServerApiControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ServerService serverService;

    @Test
    @WithMockUser(roles = "VIEWER")
    void listShouldReturnServersForAuthenticatedUser() {
        Server server = new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, null);
        when(serverService.listActive()).thenReturn(List.of(server));

        restTestClient.get()
                .uri("/api/servers")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listShouldRequireAuthentication() {
        restTestClient.get()
                .uri("/api/servers")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void detailShouldReturn404WhenServerMissing() {
        when(serverService.get(999L)).thenThrow(new ServerNotFoundException(999L));

        restTestClient.get()
                .uri("/api/servers/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
