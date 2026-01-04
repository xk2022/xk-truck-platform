package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "建立權限請求")
public class UpmsPermissionCreateReq {

    @NotBlank
    @Size(max = 80)
    @Schema(description = "所屬系統代碼", example = "UPMS")
    private String systemCode;

    @NotBlank
    @Size(max = 120)
    @Schema(description = "資源代碼", example = "ORDER")
    private String resourceCode;

    @NotBlank
    @Size(max = 40)
    @Schema(description = "動作代碼", example = "READ")
    private String actionCode;

    @NotBlank
    @Size(max = 200)
    @Schema(description = "權限名稱（顯示用）", example = "訂單-查詢")
    private String name;

    @Size(max = 500)
    @Schema(description = "權限描述")
    private String description;

    @NotNull
    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled = true;

    @Schema(description = "排序（前端顯示用）", example = "0")
    private Integer sortOrder = 0;
}
