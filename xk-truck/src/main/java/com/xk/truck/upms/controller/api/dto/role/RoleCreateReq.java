package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "建立角色請求")
public class RoleCreateReq {
    private String code;
    private String name;
    private Set<String> permissionCodes;
}
