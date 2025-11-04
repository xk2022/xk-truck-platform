package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Schema(description = "使用者回傳資料")
@Data
public class UserResp {
    private UUID uuid;
    private String username;
    private Boolean enabled;
    private Set<String> roles;
}
