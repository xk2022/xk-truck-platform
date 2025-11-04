package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "更新角色請求")
public class RoleUpdateReq {
    private String name;
    private Set<String> permissionCodes;
}
