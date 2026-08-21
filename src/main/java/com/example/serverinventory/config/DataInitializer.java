package com.example.serverinventory.config;

import com.example.serverinventory.domain.Role;
import com.example.serverinventory.domain.User;
import com.example.serverinventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 최초 실행 시 로그인할 계정이 하나도 없으면 콘솔로만 안내되는 임시 admin 계정을 만들어둔다.
 * 실제 운영에서는 이 클래스를 제거하고 회원가입으로만 계정을 만들어야 한다.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("admin", encoder.encode("admin1234!"), "관리자", Role.ADMIN));
                log.info("초기 관리자 계정을 생성했습니다. username=admin, password=admin1234!");
            }
        };
    }
}
