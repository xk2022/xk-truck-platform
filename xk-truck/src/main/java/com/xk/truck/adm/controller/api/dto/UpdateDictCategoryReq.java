package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ===============================================================
 * DTO          : UpdateDictCategoryReq
 * Layer        : Controller → Service (Request DTO)
 * Purpose      : 更新「字典分類（Dictionary Category）」請求資料
 * ===============================================================
 * Design Notes
 * - 採用 Patch 語義：
 *   → 只更新「非 null」欄位
 * - 不包含 id（由 PathVariable 傳入）
 * - 是否允許修改 code，由 Service 層決定
 * ===============================================================
 */
@Data
@Schema(
        name = "UpdateDictCategoryReq",
        description = "更新字典分類（Dictionary Category）的請求資料（Patch）"
)
public class UpdateDictCategoryReq {

    /**
     * 分類代碼（穩定識別鍵）
     * - 是否允許修改，交由 Service 控制
     */
    @Size(max = 64)
    @Schema(
            description = "字典分類代碼（穩定識別鍵，是否允許修改由系統規則決定）",
            example = "upms.user.status",
            maxLength = 64
    )
    private String code;

    /**
     * 分類顯示名稱
     */
    @Size(max = 128)
    @Schema(
            description = "字典分類顯示名稱",
            example = "使用者狀態",
            maxLength = 128
    )
    private String name;

    /**
     * 備註說明
     */
    @Size(max = 255)
    @Schema(
            description = "備註說明（僅供管理者閱讀，不影響程式邏輯）",
            example = "調整顯示名稱，與新 UI 文案一致",
            maxLength = 255
    )
    private String remark;

    /**
     * 啟用狀態
     * - 常用於後台快速切換（enable / disable）
     */
    @Schema(
            description = "是否啟用該字典分類",
            example = "true"
    )
    private Boolean enabled;
}
