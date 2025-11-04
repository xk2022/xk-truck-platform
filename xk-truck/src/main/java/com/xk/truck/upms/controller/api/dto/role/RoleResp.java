package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "角色回傳資料")
public class RoleResp {
    private UUID uuid;
    private String code;
    private String name;
    private Set<String> permissionCodes;
}
