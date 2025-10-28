package com.xk.truck.api;

import com.xk.truck.api.dto.*;
import com.xk.base.security.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// 注意：不要 import Spring 的 RequestBody 到這段 @Operation 用的命名空間

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*; // 這裡會帶入 Spring 的 RequestBody，僅用於方法參數

@Tag(name = "Auth", description = "認證相關 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @Operation(
            summary = "使用者登入",
            description = "使用帳號密碼登入並取得 JWT Bearer Token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( // ✅ 指定 OpenAPI 的 RequestBody
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            mediaType = "application/json"
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "登入成功，回傳 JWT",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "認證失敗（帳密錯誤或帳號停用）"
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@org.springframework.web.bind.annotation.RequestBody LoginRequest req) { // ✅ 這裡用 Spring 的
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        String[] roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .toArray(String[]::new);

        String token = jwtService.generate(auth.getName(), roles, null);
        long exp = 60L * 60L * 2L; // 120 分鐘
        return ResponseEntity.ok(new LoginResponse(token, exp));
    }
}
