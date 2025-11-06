package com.xk.truck.config;

import com.xk.truck.upms.infra.security.DbUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class TruckAuthAdapter {

    private final DbUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder; // 由 xk-base 或本模組提供，務必只有一顆

    /**
     * 命名為 truckDaoAuthenticationProvider，避免與 xk-base 的 daoAuthenticationProvider 同名衝突。
     * 兩顆 Provider 可同時存在；全域 AuthenticationManager 會自動收集。
     */
    @Bean(name = "truckDaoAuthenticationProvider")
    public DaoAuthenticationProvider truckDaoAuthenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        // 不暴露使用者是否存在
        p.setHideUserNotFoundExceptions(true);
        return p;
    }

    /**
     * 若容器內尚無 AuthenticationManager，就用我們自己的 ProviderManager。
     * 注意：這會只掛上 truckDaoAuthenticationProvider；若你還有其他 Provider，
     * 可以一起放進 List。
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider truckDaoAuthenticationProvider) {
        return new ProviderManager(List.of(truckDaoAuthenticationProvider));
    }
}
