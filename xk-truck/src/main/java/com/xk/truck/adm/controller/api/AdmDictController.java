package com.xk.truck.adm.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.adm.controller.api.dto.*;
import com.xk.truck.adm.domain.service.AdmDictCategoryService;
import com.xk.truck.adm.domain.service.AdmDictItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : AdmDictController
 * Layer            : Controller (REST API)
 * Purpose          : ADM 字典檔管理（Dictionary Category / Item）
 * ===============================================================
 * <p>
 * Design Principles
 * - 統一回傳 ApiResult（成功/失敗格式由共用 exception handler 處理）
 * - Controller 僅做：
 * 1) 路由與參數綁定
 * 2) DTO 驗證（@Valid）
 * 3) 呼叫 Service
 * - Controller 不做業務邏輯：
 * - 不檢查唯一性/exists
 * - 不處理排序規則
 * - 不做關聯刪除/引用檢查
 * → 以上全部交給 Service（BusinessException）
 * <p>
 * Routing Notes
 * - GET /by-code/{code}：避免與 PATCH/DELETE /{id} 的路徑衝突
 * （Spring MVC 只看 path pattern，不看參數型別）
 * <p>
 * MVP Scope
 * - Category：findAll 直接回 List（供 Master-Detail 左側快速載入）
 * - Item：findAllByCategoryId 回 List（供右側 table 顯示）
 * - 若資料量變大（>500/1000）建議升級為 pageable/query
 * ===============================================================
 */
@Tag(name = "ADM - Dictionary API", description = "字典檔管理（分類與項目）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adm/dictionaries")
public class AdmDictController {

    private final AdmDictCategoryService categoryService;
    private final AdmDictItemService itemService;

    // ===============================================================
    // Category（字典分類）
    // ===============================================================

    /* -------------------------------------------------------------
     * Create
     * ------------------------------------------------------------- */

    /**
     * 建立字典分類
     * POST /api/adm/dictionaries
     */
    @Operation(summary = "建立字典分類")
    @PostMapping
    public ApiResult<DictCategoryResp> createCategory(
            @Valid @RequestBody CreateDictCategoryReq req
    ) {
        return ApiResult.success(categoryService.create(req));
    }

    /* -------------------------------------------------------------
     * Read
     * ------------------------------------------------------------- */

    /**
     * 依 code 取得單一字典分類
     * GET /api/adm/dictionaries/by-code/{code}
     */
    @Operation(
            summary = "取得單一字典分類（依 code）",
            description = "依 code 查詢指定字典分類。採 /by-code 避免與 /{id} 衝突。"
    )
    @GetMapping("/by-code/{code}")
    public ApiResult<DictCategoryResp> findByCode(
            @PathVariable("code") String code
    ) {
        return ApiResult.success(categoryService.findByCode(code));
    }

    /**
     * 查詢字典分類列表（MVP：一次取回全部）
     * GET /api/adm/dictionaries
     * <p>
     * Weakness / Future
     * - 類別數量過大會變慢 → v2 改為 query/pageable
     */
    @Operation(summary = "查詢字典分類列表（MVP：一次取回全部）")
    @GetMapping
    public ApiResult<List<DictCategoryResp>> findAllCategories() {
        return ApiResult.success(categoryService.findAll());
    }

    /* -------------------------------------------------------------
     * Update
     * ------------------------------------------------------------- */

    /**
     * 更新字典分類（Patch）
     * PATCH /api/adm/dictionaries/{id}
     * <p>
     * Patch semantics
     * - 只更新 request 內非 null 欄位
     * - code 若更新需做唯一性檢查（Service）
     */
    @Operation(summary = "更新字典分類（Patch）")
    @PatchMapping("/{id}")
    public ApiResult<DictCategoryResp> updateCategory(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateDictCategoryReq req
    ) {
        return ApiResult.success(categoryService.update(id, req));
    }

    /* -------------------------------------------------------------
     * Delete
     * ------------------------------------------------------------- */

    /**
     * 刪除字典分類（MVP：可硬刪）
     * DELETE /api/adm/dictionaries/{id}
     * <p>
     * ⚠️ Weakness
     * - 若分類底下已有 items 或被其他資料引用：
     * 1) FK constraint 可能爆
     * 2) 或造成歷史資料顯示 '-'（label 查不到）
     * <p>
     * Recommended (v2/v3)
     * - 改為停用 enabled=false
     * - 或刪除前做引用檢查（Service）
     */
    @Operation(
            summary = "刪除字典分類",
            description = "MVP 可硬刪；正式環境建議改停用或加引用檢查"
    )
    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteCategory(
            @PathVariable("id") UUID id
    ) {
        categoryService.delete(id);
        return ApiResult.success();
    }

    // ===============================================================
    // Item（字典項目）
    // ===============================================================

    /* -------------------------------------------------------------
     * Create
     * ------------------------------------------------------------- */

    /**
     * 建立字典項目
     * POST /api/adm/dictionaries/{categoryId}/items
     * <p>
     * Typical rules (Service)
     * - itemCode 在同一分類下唯一
     * - sortOrder 未給：max(sortOrder)+1（或採預設策略）
     * - enabled 預設 true
     */
    @Operation(summary = "建立字典項目")
    @PostMapping("/{categoryId}/items")
    public ApiResult<DictItemResp> createItem(
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody CreateDictItemReq req
    ) {
        return ApiResult.success(itemService.create(categoryId, req));
    }

    /* -------------------------------------------------------------
     * Read
     * ------------------------------------------------------------- */

    /**
     * 取得字典項目列表（依分類 UUID）
     * GET /api/adm/dictionaries/{categoryId}/items
     * <p>
     * v2（資料量大再做）
     * - keyword/enabled/page/size/sort
     */
    @Operation(summary = "取得字典項目列表（依字典分類 UUID）")
    @GetMapping("/{categoryId}/items")
    public ApiResult<List<DictItemResp>> findAllItemsByCategoryId(
            @PathVariable("categoryId") UUID categoryId
    ) {
        return ApiResult.success(itemService.findAllByCategoryId(categoryId));
    }

    /* -------------------------------------------------------------
     * Update
     * ------------------------------------------------------------- */

    /**
     * 更新字典項目（Patch）
     * PATCH /api/adm/dictionaries/items/{itemId}
     * <p>
     * Typical rules (Service)
     * - itemCode 若更新：需檢查同 category 下唯一
     * - itemLabel 不可空
     * - sortOrder 可更新（手動排序/微調）
     */
    @Operation(summary = "更新字典項目（Patch）")
    @PatchMapping("/items/{itemId}")
    public ApiResult<DictItemResp> updateItem(
            @PathVariable("itemId") UUID itemId,
            @Valid @RequestBody UpdateDictItemReq req
    ) {
        return ApiResult.success(itemService.update(itemId, req));
    }

    /* -------------------------------------------------------------
     * Delete
     * ------------------------------------------------------------- */

    /**
     * 刪除字典項目（MVP：可硬刪）
     * DELETE /api/adm/dictionaries/items/{itemId}
     * <p>
     * ⚠️ Weakness
     * - 若被歷史資料引用，硬刪可能造成：
     * 1) FK constraint 爆掉
     * 2) 歷史資料 label 顯示 '-'
     * <p>
     * Recommended (v2/v3)
     * - 改為停用 enabled=false
     * - 或刪除前做引用檢查（Service）
     */
    @Operation(summary = "刪除字典項目")
    @DeleteMapping("/items/{itemId}")
    public ApiResult<Void> deleteItem(
            @PathVariable("itemId") UUID itemId
    ) {
        itemService.delete(itemId);
        return ApiResult.success();
    }

    /* -------------------------------------------------------------
     * Sort (Batch Patch)
     * ------------------------------------------------------------- */

    /**
     * 批次更新排序（拖曳排序 / 批次調整）
     * PATCH /api/adm/dictionaries/items/sort
     * <p>
     * body example:
     * {
     * "categoryId": "...",
     * "orders": [
     * {"id":"...", "sortOrder": 1},
     * {"id":"...", "sortOrder": 2}
     * ]
     * }
     */
    @Operation(
            summary = "批次更新字典項目排序",
            description = "一次更新多筆 item 的 sortOrder（拖曳排序/批次調整用）"
    )
    @PatchMapping("/items/sort")
    public ApiResult<Void> updateItemSort(
            @Valid @RequestBody SortPatchDictItemReq req
    ) {
        itemService.updateItemSort(req);
        return ApiResult.success();
    }
}
