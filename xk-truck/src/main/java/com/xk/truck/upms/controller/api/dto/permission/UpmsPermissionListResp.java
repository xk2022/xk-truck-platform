package com.xk.truck.upms.controller.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * DTO : UpmsPermissionListResp
 * Purpose : 權限列表頁回傳用 DTO（後台）
 * ===============================================================
 * <p>
 * 設計原則：
 * 1) 僅包含「列表頁需要的欄位」
 * 2) 不包含任何 Entity / Lazy 關聯
 * 3) 欄位名稱與前端顯示語意一致
 * 4) 與 UpmsPermissionService.pageForList() 對齊
 * <p>
 * ⚠ 注意：
 * - 若未來需要顯示 Role 使用數、是否被使用
 * 建議透過額外 query / projection 補，不要塞進 Entity
 * ===============================================================
 */
@Data
@Schema(description = "UPMS 權限列表回應 DTO")
public class UpmsPermissionListResp {

    // ===============================================================
    // Identity
    // ===============================================================

    @Schema(description = "權限 UUID")
    private UUID id;

    @Schema(description = "權限代碼（唯一）")
    private String code;

    // ===============================================================
    // Display
    // ===============================================================

    @Schema(description = "權限名稱")
    private String name;

    @Schema(description = "權限類型（如 API / MENU / BUTTON，可選）")
    private String type;

    @Schema(description = "所屬系統代碼（如 UPMS / FMS / TOM）")
    private String systemCode;

    // ===============================================================
    // Status / Order
    // ===============================================================

    @Schema(description = "是否啟用")
    private Boolean enabled;

    @Schema(description = "排序值")
    private Integer sortOrder;

    // ===============================================================
    // Remark / Audit
    // ===============================================================

    @Schema(description = "備註")
    private String remark;

    @Schema(description = "建立時間")
    private ZonedDateTime createdTime;

    @Schema(description = "最後更新時間")
    private ZonedDateTime updatedTime;
}
