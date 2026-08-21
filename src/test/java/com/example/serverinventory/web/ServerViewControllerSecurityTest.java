package com.example.serverinventory.web;

import com.example.serverinventory.config.WebSecurityConfig;
import com.example.serverinventory.repository.UserRepository;
import com.example.serverinventory.service.ServerService;
import com.example.serverinventory.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(ServerViewController.class)
@AutoConfigureRestTestClient
@Import(WebSecurityConfig.class)
class ServerViewControllerSecurityTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ServerService serverService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StorageService storageService;

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCannotOpenRegistrationForm() {
        restTestClient.get()
                .uri("/servers/new")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenRegistrationForm() {
        restTestClient.get()
                .uri("/servers/new")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void viewerCanListServers() {
        restTestClient.get()
                .uri("/servers")
                .exchange()
                .expectStatus().isOk();
    }
}
