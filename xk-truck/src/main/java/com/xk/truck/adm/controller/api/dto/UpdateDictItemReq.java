package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ===============================================================
 * DTO          : UpdateDictItemReq
 * Layer        : Controller → Service (Request DTO)
 * Purpose      : 更新「字典項目（Dictionary Item）」請求資料（Patch）
 * ===============================================================
 * Patch Semantics
 * - 只更新「非 null」欄位
 * - itemCode 若有更新：需在同一分類(category)下保持唯一（由 Service 檢查）
 * - itemLabel 若有更新：不可為空字串（由 Service 檢查 trim 後長度）
 *
 * Notes
 * - 不包含 categoryId：由 itemId 找回 entity 後取得 categoryUuid
 * - 不包含 enabled：若要提供停用/啟用，建議：
 *   A) 仍使用 Patch + enabled 欄位（可開放）
 *   B) 或做專用 endpoint：PATCH /items/{id}/enabled?enabled=true
 * ===============================================================
 */
@Data
@Schema(
        name = "UpdateDictItemReq",
        description = "更新字典項目（Dictionary Item）的請求資料（Patch：僅更新非 null 欄位）"
)
public class UpdateDictItemReq {

    @Size(max = 64)
    @Schema(
            description = "字典項目代碼（同一分類下唯一；建議慎改）",
            example = "ACCEPTED",
            maxLength = 64
    )
    private String itemCode;

    @Size(max = 128)
    @Schema(
            description = "字典項目顯示名稱",
            example = "已接受",
            maxLength = 128
    )
    private String itemLabel;

    @Schema(
            description = "排序值（越小越前）",
            example = "20"
    )
    private Integer sortOrder;

    @Schema(
            description = "啟用狀態（若你允許 inline 切換，可保留；不想開放就拿掉此欄位並改做專用 API）",
            example = "true"
    )
    private Boolean enabled;

    @Size(max = 255)
    @Schema(
            description = "備註說明（僅供管理者閱讀，不影響程式邏輯）",
            example = "對應舊系統 ORDER_STATUS.ACCEPTED",
            maxLength = 255
    )
    private String remark;
}
