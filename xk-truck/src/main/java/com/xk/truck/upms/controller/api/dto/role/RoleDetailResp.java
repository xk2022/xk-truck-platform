package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Schema(description = "角色詳情回應（含權限）")
public class RoleDetailResp {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
    private Set<String> permissionCodes;
}
