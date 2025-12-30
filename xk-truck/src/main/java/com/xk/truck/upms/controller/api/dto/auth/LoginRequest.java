package com.xk.truck.upms.controller.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求 DTO
 */
@Schema(name = "LoginRequest", description = "登入請求：使用者帳密")
@Data
public class LoginRequest {

    @Schema(description = "帳號", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String username;

    @Schema(description = "密碼", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;
}
