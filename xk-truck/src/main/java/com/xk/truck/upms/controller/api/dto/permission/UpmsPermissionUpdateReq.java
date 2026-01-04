package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新權限請求")
public class UpmsPermissionUpdateReq {

    @Size(max = 200)
    @Schema(description = "權限名稱（顯示用）", example = "訂單-查詢")
    private String name;

    @Size(max = 500)
    @Schema(description = "權限描述")
    private String description;

    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序（前端顯示用）", example = "0")
    private Integer sortOrder;
}
