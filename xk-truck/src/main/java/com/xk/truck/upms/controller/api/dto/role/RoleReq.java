package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "建立/修改角色請求")
public record RoleReq(String code, String name, Boolean enabled) {
}
