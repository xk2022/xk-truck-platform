package com.xk.truck.api;

import com.xk.base.security.JwtService;
import com.xk.base.web.ApiResult;
import com.xk.truck.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Auth", description = "認證與使用者相關 API")
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Operation(summary = "使用者登入", description = "使用帳號密碼登入並取得 JWT Bearer Token")
    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            String[] roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.replace("ROLE_", ""))
                    .toArray(String[]::new);

            long exp = 2 * 60 * 60L;
            String token = jwtService.generate(auth.getName(), roles, Map.of("exp", exp));

            return ResponseEntity.ok(ApiResult.success(new LoginResponse(token, exp), "登入成功"));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤", null));
        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(ApiResult.failure(HttpStatus.FORBIDDEN, "帳號已停用", null));
        }
    }

    @Operation(summary = "取得目前登入使用者資訊")
    @GetMapping("/me")
    public ApiResult<?> whoAmI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ApiResult.failure(HttpStatus.UNAUTHORIZED, "尚未登入或 Token 無效", null);
        }

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();

        return ApiResult.success(Map.of("username", auth.getName(), "roles", roles), "目前登入資訊");
    }

    @Operation(summary = "更新 Token", description = "使用現有 Token 取得新 Token，有效期重新計算")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<Map<String, Object>>> refreshToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "缺少 Bearer Token", null));
        }

        String oldToken = header.substring(7);
        try {
            var claims = jwtService.parse(oldToken).getPayload();
            String username = claims.getSubject();
            List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());
            String newToken = jwtService.generate(username, roles.toArray(String[]::new), null);

            return ResponseEntity.ok(ApiResult.success(
                    Map.of("token", newToken, "type", "Bearer"), "Token 已更新"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "Token 無效或已過期", e.getMessage()));
        }
    }
}
