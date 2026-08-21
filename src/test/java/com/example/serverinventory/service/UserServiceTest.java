package com.example.serverinventory.service;

import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.SelfRoleChangeException;
import com.example.serverinventory.exception.UserNotFoundException;
import com.example.serverinventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @Test
    void changeRoleShouldUpdateTargetUserRole() {
        userService = new UserService(userRepository, passwordEncoder);
        User target = new User("viewer1", "encoded", "뷰어", Role.VIEWER);
        ReflectionTestUtils.setField(target, "id", 2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        userService.changeRole(2L, Role.ADMIN, "admin");

        assertThat(target.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void changeRoleShouldRejectSelfChange() {
        userService = new UserService(userRepository, passwordEncoder);
        User admin = new User("admin", "encoded", "관리자", Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.changeRole(1L, Role.VIEWER, "admin"))
                .isInstanceOf(SelfRoleChangeException.class);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void changeRoleShouldThrowWhenUserMissing() {
        userService = new UserService(userRepository, passwordEncoder);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeRole(999L, Role.ADMIN, "admin"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
