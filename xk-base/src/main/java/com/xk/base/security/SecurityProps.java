package com.xk.base.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProps {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    /**
     * 放行路徑（prefix 支援 /**）
     */
    private List<String> permitAll = new ArrayList<>(List.of(
            "/auth/login",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    ));

    @Data
    public static class Jwt {
        /**
         * 建議使用 Base64 編碼、長度 >= 32 bytes（HS256）
         */
        private String secret;

        /**
         * JWT issuer
         */
        private String issuer = "xk-truck";

        /**
         * 預設存活時間（預設 120 分鐘）
         */
        @DurationUnit(ChronoUnit.MINUTES)
        private Duration expiry = Duration.ofMinutes(120);

        // 若你仍想用分鐘數，也可保留 Long expiryMinutes，搭配 converter 在 service 轉成 Duration
    }

    @Getter
    @Setter
    public static class Cors {
        /**
         * 注意：若要 withCredentials=true，不能用 "*"，請改成明確網域
         */
        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
        private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
        private List<String> exposedHeaders = new ArrayList<>(List.of("Authorization"));
        // 可視需求再加：private Boolean allowCredentials = false;
    }
}
