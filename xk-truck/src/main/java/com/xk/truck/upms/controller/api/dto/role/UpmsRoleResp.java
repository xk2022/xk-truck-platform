package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "角色基本回應")
public class UpmsRoleResp {
    private UUID uuid;
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
}
