package com.xk.truck.upms.controller.api.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * 目前登入者資訊 DTO
 */
@Data
@Builder
public class MeResponse {

    private UUID userId;
    private String username;

    // Profile
    private String name;
    private String nickName;
    private String email;
    private String phone;
    private String avatarUrl;

    // 狀態
    private Boolean enabled;
    private Boolean locked;
    private Integer loginFailCount;
    private LocalDateTime lastLoginAt;

    // 授權
    private Set<String> roleCodes;
    private Set<String> permissionCodes;
}
