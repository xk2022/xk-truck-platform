package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ===============================================================
 * DTO : UpmsPermissionQuery
 * Purpose : 權限列表查詢條件
 * ===============================================================
 * <p>
 * 設計原則：
 * 1) 純查詢條件，不含任何業務邏輯
 * 2) 欄位命名與查詢語意一致
 * 3) 可直接對應 Specification 條件
 * <p>
 * 對應 Service：
 * - UpmsPermissionService.buildPermissionSpec()
 * <p>
 * 擴充方式：
 * - 新增欄位 → 在 spec 中加 predicate
 * - 不影響既有 API
 * ===============================================================
 */
@Data
@Schema(description = "UPMS 權限查詢條件")
public class UpmsPermissionQuery {

    // ===============================================================
    // Keyword
    // ===============================================================

    @Schema(
            description = "關鍵字（模糊查詢 code / name）",
            example = "USER"
    )
    private String keyword;

    // ===============================================================
    // Status
    // ===============================================================

    @Schema(description = "是否啟用")
    private Boolean enabled;

    // ===============================================================
    // Type / System
    // ===============================================================

    @Schema(
            description = "權限類型（如 API / MENU / BUTTON）",
            example = "API"
    )
    private String type;

    @Schema(
            description = "所屬系統代碼",
            example = "UPMS"
    )
    private String systemCode;
}
