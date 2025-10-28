package com.xk.base.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security")
public class SecurityProps {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    /**
     * 放行路徑（prefix 支援 /**）
     */
    private List<String> permitAll = List.of("/auth/login", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health");

    @Getter
    @Setter
    public static class Jwt {
        /**
         * 至少 32 字以上隨機字串（HMAC-256）
         */
        private String secret = "change-me-32chars-min-change-me-32chars-min";
        private String issuer = "xk-platform";
        private long expiryMinutes = 120;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization");
    }
}
