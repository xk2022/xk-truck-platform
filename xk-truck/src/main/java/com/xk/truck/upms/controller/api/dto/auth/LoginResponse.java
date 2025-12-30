package com.xk.truck.upms.controller.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;


/**
 * 登入回應 DTO：JWT + 使用者資訊
 */
@Schema(name = "LoginResponse", description = "登入回應：JWT 與秒數")
@Data
@Builder
public class LoginResponse {

    /**
     * JWT Access Token
     */
    @Schema(description = "JWT Bearer Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * token 類型，預設 Bearer
     */
    private String tokenType = "Bearer";

    /**
     * 目前登入者資訊
     */
    private MeResponse me;

//    @Schema(description = "Token 有效秒數", example = "7200")
//    long expiresInSeconds
}
