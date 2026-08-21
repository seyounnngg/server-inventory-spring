package com.example.serverinventory.web;

import com.example.serverinventory.exception.DuplicateUsernameException;
import com.example.serverinventory.service.UserService;
import com.example.serverinventory.web.dto.SignupForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    private AuthController controller() {
        return new AuthController(userService);
    }

    @Test
    void signupShouldRedirectToLoginOnSuccess() {
        SignupForm form = new SignupForm();
        form.setUsername("newbie");
        form.setPassword("password1");
        form.setName("새사람");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "signupForm");

        String view = controller().signup(form, bindingResult);

        assertThat(view).isEqualTo("redirect:/login?signup");
    }

    @Test
    void signupShouldReRenderFormWhenUsernameAlreadyExists() {
        SignupForm form = new SignupForm();
        form.setUsername("existing");
        form.setPassword("password1");
        form.setName("이름");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "signupForm");
        when(userService.signup(any())).thenThrow(new DuplicateUsernameException("existing"));

        String view = controller().signup(form, bindingResult);

        assertThat(view).isEqualTo("signup");
        assertThat(bindingResult.hasFieldErrors("username")).isTrue();
    }

    @Test
    void signupShouldReRenderFormWhenValidationAlreadyFailed() {
        SignupForm form = new SignupForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "signupForm");
        bindingResult.rejectValue("username", "NotBlank");

        String view = controller().signup(form, bindingResult);

        assertThat(view).isEqualTo("signup");
    }
}
