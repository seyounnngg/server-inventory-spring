package com.example.serverinventory.web;

import com.example.serverinventory.domain.Role;
import com.example.serverinventory.exception.SelfRoleChangeException;
import com.example.serverinventory.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.listAll());
        return "admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                              Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            userService.changeRole(id, role, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "권한을 " + role + "(으)로 변경했습니다.");
        } catch (SelfRoleChangeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
