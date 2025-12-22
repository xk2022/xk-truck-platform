package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新權限請求")
public class UpmsPermissionUpdateReq {
    private String code;
    private String name;
    private String description;
}
