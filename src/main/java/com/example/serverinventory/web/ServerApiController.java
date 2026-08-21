package com.example.serverinventory.web;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.service.ServerService;
import com.example.serverinventory.web.dto.ServerResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/servers")
public class ServerApiController {

    private final ServerService serverService;

    public ServerApiController(ServerService serverService) {
        this.serverService = serverService;
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
}
