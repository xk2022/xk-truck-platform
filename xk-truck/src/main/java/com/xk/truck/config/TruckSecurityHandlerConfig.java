package com.xk.truck.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xk.base.web.ApiResult;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class TruckSecurityHandlerConfig {

    // ✅ 注入 Spring 管理、已註冊 JavaTimeModule 的 ObjectMapper
    private final ObjectMapper objectMapper;

    /**
     * 未登入或 Token 無效 → 401
     */
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            var body = ApiResult.failure(HttpStatus.UNAUTHORIZED, "未授權，請先登入", null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        };
    }


    /**
     * 已登入但權限不足 → 403
     */
    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            var body = ApiResult.failure(HttpStatus.FORBIDDEN, "沒有存取此資源的權限", null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        };
    }
}
