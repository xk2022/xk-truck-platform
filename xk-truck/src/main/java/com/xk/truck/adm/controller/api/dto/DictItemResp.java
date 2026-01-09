package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * DTO          : DictItemResp
 * Layer        : Service → Controller → Frontend
 * Purpose      : 字典項目（Dictionary Item）回傳資料
 *
 * Design Notes
 * - 用於後台 Items Table、下拉選單、狀態顯示
 * - categoryId 明確帶出，方便前端比對與操作
 * ===============================================================
 */
@Data
@Schema(
        name = "DictItemResp",
        description = "字典項目（Dictionary Item）回傳資料"
)
public class DictItemResp {

    @Schema(
            description = "字典項目 UUID",
            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID id;

    @Schema(
            description = "所屬字典分類 UUID",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID categoryId;

    @Schema(
            description = "字典項目代碼（Value / Code）",
            example = "ACCEPTED"
    )
    private String itemCode;

    @Schema(
            description = "字典項目顯示名稱（Label）",
            example = "已接受"
    )
    private String itemLabel;

    @Schema(
            description = "排序值（越小越前）",
            example = "20"
    )
    private Integer sortOrder;

    @Schema(
            description = "是否啟用",
            example = "true"
    )
    private Boolean enabled;

    @Schema(
            description = "備註說明（僅供管理者閱讀）",
            example = "對應舊系統 ORDER_STATUS.ACCEPTED"
    )
    private String remark;

    @Schema(
            description = "建立時間",
            example = "2025-01-01T10:30:00"
    )
    private LocalDateTime createdTime;

    @Schema(
            description = "最後更新時間",
            example = "2025-01-05T14:20:00"
    )
    private LocalDateTime updatedTime;
}
