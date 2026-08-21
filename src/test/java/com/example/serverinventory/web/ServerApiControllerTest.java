package com.example.serverinventory.web;

import com.example.serverinventory.config.WebSecurityConfig;
import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.repository.UserRepository;
import com.example.serverinventory.service.ServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebMvcTest(ServerApiController.class)
@AutoConfigureRestTestClient
@Import(WebSecurityConfig.class)
class ServerApiControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ServerService serverService;

    @MockitoBean
    private UserRepository userRepository;

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

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createShouldReturn201ForAdmin() {
        User admin = new User("admin", "encoded", "관리자", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        Server created = new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, admin);
        ReflectionTestUtils.setField(created, "id", 1L);
        when(serverService.create(any(), any())).thenReturn(created);

        restTestClient.post()
                .uri("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"hostname":"lab-server-01","ipAddress":"192.168.0.10","port":22,"status":"RUNNING"}
                        """)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createShouldBeForbiddenForViewer() {
        restTestClient.post()
                .uri("/api/servers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"hostname":"lab-server-01","ipAddress":"192.168.0.10","port":22,"status":"RUNNING"}
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShouldReturnUpdatedServer() {
        Server updated = new Server("lab-server-01-renamed", "192.168.0.10", 22, ServerStatus.RUNNING, null);
        when(serverService.update(anyLong(), any())).thenReturn(updated);

        restTestClient.put()
                .uri("/api/servers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"hostname":"lab-server-01-renamed","ipAddress":"192.168.0.10","port":22,"status":"RUNNING"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShouldReturn204() {
        restTestClient.delete()
                .uri("/api/servers/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
