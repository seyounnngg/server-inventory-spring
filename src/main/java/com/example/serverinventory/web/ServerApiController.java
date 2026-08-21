package com.example.serverinventory.web;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.repository.UserRepository;
import com.example.serverinventory.service.ServerService;
import com.example.serverinventory.web.dto.ServerForm;
import com.example.serverinventory.web.dto.ServerResponse;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/servers")
public class ServerApiController {

    private final ServerService serverService;
    private final UserRepository userRepository;

    public ServerApiController(ServerService serverService, UserRepository userRepository) {
        this.serverService = serverService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public CollectionModel<EntityModel<ServerResponse>> list() {
        List<EntityModel<ServerResponse>> servers = serverService.listActive().stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(servers,
                linkTo(methodOn(ServerApiController.class).list()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<ServerResponse> detail(@PathVariable Long id) {
        Server server = serverService.get(id);
        return toModel(server);
    }

    @PostMapping
    public ResponseEntity<EntityModel<ServerResponse>> create(@Valid @RequestBody ServerForm form,
                                                                Authentication authentication) {
        User owner = currentUser(authentication);
        Server server = serverService.create(form, owner);
        EntityModel<ServerResponse> model = toModel(server);
        URI location = linkTo(methodOn(ServerApiController.class).detail(server.getId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public EntityModel<ServerResponse> update(@PathVariable Long id, @Valid @RequestBody ServerForm form) {
        Server server = serverService.update(id, form);
        return toModel(server);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serverService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ServerResponse> toModel(Server server) {
        EntityModel<ServerResponse> model = EntityModel.of(ServerResponse.from(server),
                linkTo(methodOn(ServerApiController.class).detail(server.getId())).withSelfRel(),
                linkTo(methodOn(ServerApiController.class).list()).withRel("servers"));

        if (hasAdminRole()) {
            model.add(Link.of("/servers/" + server.getId() + "/edit", "edit"));
        }
        return model;
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인한 사용자를 찾을 수 없습니다."));
    }
}
