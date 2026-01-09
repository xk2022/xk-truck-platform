package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ===============================================================
 * DTO : CreateDictCategoryReq
 * Layer: Controller → Service (Request DTO)
 * Purpose:
 * - 建立「字典分類（Dictionary Category）」請求資料
 * <p>
 * Design Notes
 * - 僅承載「建立時必要資訊」
 * - 不包含 id / enabled / timestamps
 * → 由 Service / Entity 決定預設值
 * ===============================================================
 */
@Data
@Schema(
        name = "CreateDictCategoryReq",
        description = "建立字典分類（Dictionary Category）的請求資料"
)
public class CreateDictCategoryReq {

    /**
     * 分類代碼（穩定識別鍵）
     */
    @NotBlank
    @Size(max = 64)
    @Schema(
            description = "字典分類代碼（穩定識別鍵）",
            example = "upms.user.status",
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;

    /**
     * 分類顯示名稱
     */
    @NotBlank
    @Size(max = 128)
    @Schema(
            description = "字典分類顯示名稱（後台與下拉選單顯示用）",
            example = "使用者狀態",
            maxLength = 128,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    /**
     * 備註說明
     */
    @Size(max = 255)
    @Schema(
            description = "備註說明（僅供管理者閱讀，不影響程式邏輯）",
            example = "對應舊系統 USER_STATUS enum",
            maxLength = 255
    )
    private String remark;
}
