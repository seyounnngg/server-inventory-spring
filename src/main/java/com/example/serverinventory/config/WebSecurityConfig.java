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
                        .requestMatchers("/servers/new", "/servers/*/edit", "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/servers/**", "/api/servers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/servers/**", "/api/servers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/servers/**", "/api/servers/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/servers", true)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll)
                // H2 콘솔은 프레임 안에서 렌더링되고 개발 편의용 CSRF 예외가 필요하다.
                // /api/**는 JSON 바디를 요구하므로 단순 HTML 폼으로는 트리거될 수 없어 CSRF 예외로 둔다
                // (Thymeleaf 화면(/servers/**)은 여전히 세션 쿠키 + 폼 제출이라 CSRF 토큰을 그대로 요구한다).
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
