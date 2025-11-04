package com.xk.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 📌 審計用戶自動注入設定
 * 會自動將目前登入使用者帳號填入 BaseEntity.createdBy / updatedBy
 */
@Configuration
public class AuditAwareConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return Optional.of(auth.getName());
            }
            return Optional.of("system");
        };
    }
}
