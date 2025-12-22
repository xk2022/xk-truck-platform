package com.xk.truck.config;

import com.xk.truck.upms.domain.repository.UpmsUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordCheckRunner implements CommandLineRunner {

    private final PasswordEncoder encoder;
    private final UpmsUserRepository userRepository;

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresent(u -> {
            String raw = "admin123"; // 你實際在 login 用的密碼
            String db = u.getPassword();
            System.out.println("DB password = " + db);
            System.out.println("matches = " + encoder.matches(raw, db));
        });
    }
}
