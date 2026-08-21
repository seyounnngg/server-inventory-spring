package com.example.serverinventory.service;

import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.exception.DuplicateUsernameException;
import com.example.serverinventory.repository.UserRepository;
import com.example.serverinventory.web.dto.SignupForm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public User signup(SignupForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new DuplicateUsernameException(form.getUsername());
        }
        User user = new User(
                form.getUsername(),
                passwordEncoder.encode(form.getPassword()),
                form.getName(),
                Role.VIEWER
        );
        return userRepository.save(user);
    }
}
