package com.example.serverinventory.web;

import com.example.serverinventory.exception.DuplicateUsernameException;
import com.example.serverinventory.service.UserService;
import com.example.serverinventory.web.dto.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signupForm(SignupForm signupForm) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupForm signupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            userService.signup(signupForm);
        } catch (DuplicateUsernameException e) {
            bindingResult.rejectValue("username", "duplicate", e.getMessage());
            return "signup";
        }
        return "redirect:/login?signup";
    }
}
