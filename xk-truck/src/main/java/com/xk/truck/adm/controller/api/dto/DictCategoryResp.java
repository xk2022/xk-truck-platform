package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * DTO          : DictCategoryResp
 * Layer        : Service → Controller → Frontend
 * Purpose      : 字典分類（Dictionary Category）回傳資料
 *
 * Design Notes
 * - 提供後台管理 UI / 下拉選單 / Master-Detail 使用
 * - 不回傳 Entity，避免 JPA 欄位與關聯外洩
 * ===============================================================
 */
@Data
@Schema(
        name = "DictCategoryResp",
        description = "字典分類（Dictionary Category）回傳資料"
)
public class DictCategoryResp {

    @Schema(
            description = "字典分類 UUID",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    private UUID id;

    @Schema(
            description = "字典分類代碼（穩定識別鍵）",
            example = "upms.user.status"
    )
    private String code;

    @Schema(
            description = "字典分類顯示名稱",
            example = "使用者狀態"
    )
    private String name;

    @Schema(
            description = "是否啟用",
            example = "true"
    )
    private Boolean enabled;

    @Schema(
            description = "備註說明（僅供管理者閱讀）",
            example = "對應舊系統 USER_STATUS enum"
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
