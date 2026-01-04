package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Schema(description = "權限回傳資料")
public class UpmsPermissionResp {

    @Schema(description = "權限 UUID")
    private UUID id;

    @Schema(description = "權限代碼（SYSTEM_RESOURCE_ACTION）")
    private String code;

    @Schema(description = "系統代碼")
    private String systemCode;

    @Schema(description = "資源代碼")
    private String resourceCode;

    @Schema(description = "動作代碼")
    private String actionCode;

    @Schema(description = "分組鍵（SYSTEM_RESOURCE）")
    private String groupKey;

    @Schema(description = "權限名稱")
    private String name;

    @Schema(description = "權限描述")
    private String description;

    @Schema(description = "是否啟用")
    private Boolean enabled;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "版本號（L4-ready）")
    private Integer version;

    @Schema(description = "建立時間")
    private OffsetDateTime createdAt;

    @Schema(description = "更新時間")
    private OffsetDateTime updatedAt;
}
