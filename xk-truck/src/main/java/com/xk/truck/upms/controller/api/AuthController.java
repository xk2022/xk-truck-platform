package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.AuthService;

import com.xk.truck.upms.controller.api.dto.auth.LoginRequest;
import com.xk.truck.upms.controller.api.dto.auth.LoginResponse;

import com.xk.truck.upms.controller.api.dto.auth.MeResponse;

import com.xk.truck.upms.controller.api.dto.auth.RefreshTokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 認證與使用者相關 API
 */
@Tag(name = "Auth", description = "認證與使用者相關 API")
@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ========================
    //  登入 / 目前使用者資訊
    // ========================

    @Operation(summary = "使用者登入", description = "使用帳號密碼登入並取得 JWT Bearer Token + 使用者資訊")
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        // 登入流程交給 AuthService 處理（驗密碼 / 鎖定 / 登入失敗次數 / 角色權限 / 產 token）
        LoginResponse response = authService.login(req);
        return ApiResult.success(response);
    }

    @Operation(summary = "取得目前登入使用者資訊")
    @GetMapping("/me")
    public ApiResult<MeResponse> me() {
        // 由 AuthService 從 SecurityContext 取得目前使用者
        MeResponse me = authService.me();
        return ApiResult.success(me);
    }

    // ========================
    //  Token 續期 / 刷新
    // ========================

    @Operation(summary = "更新 Token", description = "使用現有 Token 取得新 Token，有效期重新計算")
    @PostMapping("/refresh")
    public ApiResult<RefreshTokenResponse> refreshToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return ApiResult.success(authService.refreshToken(header));
    }


//    @Operation(summary = "使用者登入", description = "使用帳號密碼登入並取得 JWT Bearer Token")
//    @PostMapping(
//            path = "/login",
//            consumes = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<ApiResult<LoginResponse>> login(@RequestBody LoginRequest req) {
//        try {
//            Authentication auth = authManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
//            );
//
//            String[] roles = auth.getAuthorities().stream()
//                    .map(GrantedAuthority::getAuthority)
//                    .map(a -> a.replaceFirst("^ROLE_", ""))
//                    .toArray(String[]::new);
//
//            Duration ttl = LOGIN_TTL; // 或改成 null 讓 JwtService 取 SecurityProps
//            String token = jwtService.generate(auth.getName(), roles, null, ttl);
//            long ttlSec = ttl.toSeconds();
//
//            return ResponseEntity.ok(ApiResult.success(new LoginResponse(token, ttlSec)));
//
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤", null));
//        } catch (DisabledException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResult.failure(HttpStatus.FORBIDDEN, "帳號已停用", null));
//        } catch (LockedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(ApiResult.failure(HttpStatus.FORBIDDEN, "帳號已鎖定", null));
//        } catch (CredentialsExpiredException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "密碼已過期", null));
//        }
//    }

//    @Operation(summary = "取得目前登入使用者資訊")
//    @GetMapping("/me")
//    public ApiResult<?> whoAmI() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        boolean invalid = (auth == null)
//                || !auth.isAuthenticated()
//                || (auth instanceof AnonymousAuthenticationToken)
//                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()));
//
//        if (invalid) {
//            return ApiResult.failure(HttpStatus.UNAUTHORIZED, "尚未登入或 Token 無效", null);
//        }
//
//        List<String> roles = auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .map(r -> r.replaceFirst("^ROLE_", ""))
//                .toList();
//
//        return ApiResult.success(
//                Map.of(
//                        "username", auth.getName(),
//                        "roles", roles
//                )
//        );
//    }

//    @Operation(summary = "更新 Token", description = "使用現有 Token 取得新 Token，有效期重新計算")
//    @PostMapping("/refresh")
//    public ResponseEntity<ApiResult<Map<String, Object>>> refreshToken(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        if (header == null || !header.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "缺少 Bearer Token", null));
//        }
//
//        String oldToken = header.substring(7);
//        try {
//            var claims = jwtService.parse(oldToken).getBody(); // 0.13.0 用 getBody()
//            String username = claims.getSubject();
//
//            String[] roles = normalizeRoles(claims.get("roles"));
//
//            // 和 login 一致的 TTL 策略（同樣可改成 null 走預設）
//            String newToken = jwtService.generate(username, roles, null, LOGIN_TTL);
//
//            return ResponseEntity.ok(ApiResult.success(
//                    Map.of("token", newToken, "type", "Bearer")
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(ApiResult.failure(HttpStatus.UNAUTHORIZED, "Token 無效或已過期", e.getMessage()));
//        }
//    }

    /**
     * 將各種可能型別（List<?> / String[] / String / null）安全轉為 String[]
     */
//    private static String[] normalizeRoles(Object rolesObj) {
//        if (rolesObj == null) return new String[0];
//
//        if (rolesObj instanceof String s) {
//            return s.isBlank() ? new String[0] : new String[]{s};
//        }
//        if (rolesObj instanceof String[] arr) {
//            return arr;
//        }
//        if (rolesObj instanceof List<?> list) {
//            List<String> out = new ArrayList<>(list.size());
//            for (Object o : list) {
//                if (o != null) out.add(o.toString());
//            }
//            return out.toArray(String[]::new);
//        }
//        // 其他型別一律 toString 後當成單一角色（保底）
//        return new String[]{rolesObj.toString()};
//    }
}
