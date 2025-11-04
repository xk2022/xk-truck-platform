package com.xk.truck.config;

import com.xk.truck.upms.infra.security.DbUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class TruckAuthAdapter {

    private final DbUserDetailsService dbUserDetailsService;
    private final PasswordEncoder passwordEncoder; // 由 xk-base 提供 BCryptPasswordEncoder

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(dbUserDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        // UsernameNotFoundException 也視為 BadCredentials（避免暴露使用者存在與否）
        p.setHideUserNotFoundExceptions(true);
        return p;
    }
}
