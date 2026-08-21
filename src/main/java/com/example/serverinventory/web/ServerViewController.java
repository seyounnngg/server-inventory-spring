package com.example.serverinventory.web;

import com.example.serverinventory.domain.Server;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.ServerNotFoundException;
import com.example.serverinventory.repository.UserRepository;
import com.example.serverinventory.service.ServerService;
import com.example.serverinventory.service.storage.StorageService;
import com.example.serverinventory.web.dto.ServerForm;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servers")
public class ServerViewController {

    private final ServerService serverService;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public ServerViewController(ServerService serverService, UserRepository userRepository, StorageService storageService) {
        this.serverService = serverService;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("servers", serverService.listActive());
        return "servers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("server", serverService.get(id));
        return "servers/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("serverForm", new ServerForm());
        return "servers/form";
    }

    @PostMapping
    public String create(@Valid ServerForm serverForm, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "servers/form";
        }
        User owner = currentUser(authentication);
        Server server = serverService.create(serverForm, owner);
        return "redirect:/servers/" + server.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Server server = serverService.get(id);
        ServerForm form = new ServerForm();
        form.setHostname(server.getHostname());
        form.setIpAddress(server.getIpAddress());
        form.setPort(server.getPort());
        form.setStatus(server.getStatus());
        model.addAttribute("serverForm", form);
        model.addAttribute("serverId", id);
        return "servers/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @Valid ServerForm serverForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("serverId", id);
            return "servers/form";
        }
        serverService.update(id, serverForm);
        return "redirect:/servers/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        serverService.softDelete(id);
        redirectAttributes.addFlashAttribute("message", "서버를 삭제했습니다.");
        return "redirect:/servers";
    }

    @PostMapping("/{id}/spec-sheet")
    public String uploadSpecSheet(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        serverService.attachSpecSheet(id, file);
        redirectAttributes.addFlashAttribute("message", "스펙시트를 업로드했습니다.");
        return "redirect:/servers/" + id;
    }

    @GetMapping("/{id}/spec-sheet")
    @ResponseBody
    public ResponseEntity<Resource> downloadSpecSheet(@PathVariable Long id) {
        Server server = serverService.get(id);
        if (server.getSpecSheetPath() == null) {
            throw new ServerNotFoundException(id);
        }
        Resource resource = storageService.loadAsResource(server.getSpecSheetPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("로그인한 사용자를 찾을 수 없습니다."));
    }
}
