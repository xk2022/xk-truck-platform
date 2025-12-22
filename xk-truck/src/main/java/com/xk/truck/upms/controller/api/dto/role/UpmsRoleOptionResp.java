package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Schema(description = "角色下拉選單選項")
@Data
public class UpmsRoleOptionResp {
    private UUID id;
    private String code;
    private String name;
}
