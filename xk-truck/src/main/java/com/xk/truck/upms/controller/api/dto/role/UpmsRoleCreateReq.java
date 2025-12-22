package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "角色建立請求")
public class UpmsRoleCreateReq {
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
    private Set<String> permissionCodes;
}
