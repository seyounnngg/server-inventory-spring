package com.example.serverinventory.service;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.ServerStatus;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.repository.ServerRepository;
import com.example.serverinventory.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private StorageService storageService;

    private ServerService serverService;

    @Test
    void getShouldThrowWhenServerNotFoundOrSoftDeleted() {
        serverService = new ServerService(serverRepository, storageService);
        when(serverRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.get(1L))
                .isInstanceOf(ServerNotFoundException.class);
    }

    @Test
    void softDeleteShouldMarkDeletedAtWithoutRemovingRow() {
        serverService = new ServerService(serverRepository, storageService);
        Server server = new Server("lab-server-01", "192.168.0.10", 22, ServerStatus.RUNNING, null);
        when(serverRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(server));

        serverService.softDelete(1L);

        assertThat(server.isDeleted()).isTrue();
        assertThat(server.getDeletedAt()).isNotNull();
    }
}
