package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ===============================================================
 * DTO          : CreateDictItemReq
 * Layer        : Controller → Service (Request DTO)
 * Purpose      : 建立「字典項目（Dictionary Item）」請求資料
 * ===============================================================
 * Design Notes
 * - 隸屬於某一 Dictionary Category（categoryId 由 PathVariable 傳入）
 * - itemCode 在同一個 category 底下必須唯一
 * - enabled 預設為 true（由 Service / Entity 控制）
 * - sortOrder 若未給，Service 可自動補 max + 1
 * ===============================================================
 */
@Data
@Schema(
        name = "CreateDictItemReq",
        description = "建立字典項目（Dictionary Item）的請求資料"
)
public class CreateDictItemReq {

    /**
     * 穩定值（Value / Code）
     */
    @NotBlank
    @Size(max = 64)
    @Schema(
            description = "字典項目代碼（同一分類下唯一）",
            example = "NEW",
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String itemCode;

    /**
     * 顯示名稱（Label）
     */
    @NotBlank
    @Size(max = 128)
    @Schema(
            description = "字典項目顯示名稱",
            example = "新建",
            maxLength = 128,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String itemLabel;

    /**
     * 排序值（越小越前）
     */
    @Schema(
            description = "排序值（越小越前；未填由系統自動補 max + 1）",
            example = "10"
    )
    private Integer sortOrder;

    /**
     * 備註
     */
    @Size(max = 255)
    @Schema(
            description = "備註說明（僅供管理者閱讀，不影響程式邏輯）",
            example = "對應舊系統 ORDER_STATUS.NEW",
            maxLength = 255
    )
    private String remark;
}
