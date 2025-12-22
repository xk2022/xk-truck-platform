package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.UUID;

@Data
@Schema(description = "權限回傳資料")
public class UpmsPermissionResp {
    private UUID uuid;
    private String code;
    private String name;
    private String description;
}
