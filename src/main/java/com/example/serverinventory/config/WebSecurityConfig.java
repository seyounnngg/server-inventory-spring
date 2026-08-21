package com.example.serverinventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login", "/signup", "/css/**", "/h2-console/**").permitAll()
                        .requestMatchers("/servers/new", "/servers/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/servers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/servers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/servers/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/servers", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll)
                // H2 콘솔은 프레임 안에서 렌더링되고 개발 편의용 CSRF 예외가 필요하다.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
