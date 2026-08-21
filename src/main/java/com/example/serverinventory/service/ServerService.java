package com.example.serverinventory.service;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.repository.ServerRepository;
import com.example.serverinventory.service.storage.StorageService;
import com.example.serverinventory.web.dto.ServerForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServerService {

    private final ServerRepository serverRepository;
    private final StorageService storageService;

    public ServerService(ServerRepository serverRepository, StorageService storageService) {
        this.serverRepository = serverRepository;
        this.storageService = storageService;
    }

    public List<Server> listActive() {
        return serverRepository.findAllByDeletedAtIsNull();
    }

    public Server get(Long id) {
        return serverRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ServerNotFoundException(id));
    }

    @Transactional
    public Server create(ServerForm form, User owner) {
        Server server = new Server(form.getHostname(), form.getIpAddress(), form.getPort(), form.getStatus(), owner);
        return serverRepository.save(server);
    }

    @Transactional
    public Server update(Long id, ServerForm form) {
        Server server = get(id);
        server.setHostname(form.getHostname());
        server.setIpAddress(form.getIpAddress());
        server.setPort(form.getPort());
        server.setStatus(form.getStatus());
        return server;
    }

    @Transactional
    public void softDelete(Long id) {
        Server server = get(id);
        server.markDeleted();
    }

    @Transactional
    public void attachSpecSheet(Long id, MultipartFile file) {
        Server server = get(id);
        String storedFilename = storageService.store(file);
        server.setSpecSheetPath(storedFilename);
    }
}
