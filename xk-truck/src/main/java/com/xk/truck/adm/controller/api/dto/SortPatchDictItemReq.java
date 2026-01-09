package com.xk.truck.adm.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * DTO          : SortPatchDictItemReq
 * Layer        : Controller → Service (Request DTO)
 * Purpose      : 批次更新字典項目排序
 *
 * API
 * - PATCH /api/adm/dictionaries/items/sort
 *
 * Body Example
 * {
 *   "categoryId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
 *   "orders": [
 *     {"id": "....", "sortOrder": 1},
 *     {"id": "....", "sortOrder": 2}
 *   ]
 * }
 *
 * Service 建議規則
 * - 驗證 orders 裡的 item 都屬於同一個 categoryId
 * - sortOrder 可允許重複（由你決定），但建議由前端拖曳產生唯一序列
 * ===============================================================
 */
@Data
@Schema(
        name = "SortPatchDictItemReq",
        description = "批次更新字典項目排序的請求資料（拖曳排序 / 批次調整用）"
)
public class SortPatchDictItemReq {

    @NotNull
    @Schema(
            description = "字典分類 UUID（所有 orders 的 item 必須屬於此分類）",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID categoryId;

    @Valid
    @NotEmpty
    @Schema(
            description = "排序清單（一次更新多筆 item 的 sortOrder）",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrderPatch> orders;

    @Data
    @Schema(
            name = "SortPatchDictItemReq.OrderPatch",
            description = "單筆字典項目的排序更新資料"
    )
    public static class OrderPatch {

        @NotNull
        @Schema(
                description = "字典項目 UUID",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private UUID id;

        @NotNull
        @Schema(
                description = "排序值（越小越前）",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Integer sortOrder;
    }
}
