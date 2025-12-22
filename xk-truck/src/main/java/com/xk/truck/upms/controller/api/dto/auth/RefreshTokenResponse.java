package com.xk.truck.upms.controller.api.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenResponse {

    private String token;

    private String type;
}
