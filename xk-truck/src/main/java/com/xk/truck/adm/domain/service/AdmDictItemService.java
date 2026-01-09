package com.xk.truck.adm.domain.service;

import com.xk.truck.adm.controller.api.dto.CreateDictItemReq;
import com.xk.truck.adm.controller.api.dto.DictItemResp;
import com.xk.truck.adm.controller.api.dto.SortPatchDictItemReq;
import com.xk.truck.adm.controller.api.dto.UpdateDictItemReq;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * UseCase Interface : AdmDictItemService
 * Layer             : Domain Service
 * Purpose           :
 * - 定義「字典項目（Dictionary Item）」的核心用例
 * - 供後台 Master-Detail 右側 Items Table 使用
 * <p>
 * Design Notes
 * - Controller 僅呼叫此介面，不直接依賴 Repository
 * - 所有業務規則（唯一性、狀態限制、刪除策略、排序驗證）皆在 Impl 處理
 * - MVP：一次載入某分類底下全部 items（通常數量不大）
 * ===============================================================
 */
public interface AdmDictItemService {

    /* ==========================================================
     * Create
     * ========================================================== */

    /**
     * 建立字典項目（隸屬某分類）
     * <p>
     * 規則（由 Service 實作）：
     * - categoryId 必須存在
     * - itemCode 在同一 category 底下唯一
     * - enabled 預設 true（若 req 未帶）
     * - sortOrder 未帶可自動補 max+1（可選）
     */
    DictItemResp create(UUID categoryId, @Valid CreateDictItemReq req);

    /* ==========================================================
     * Read
     * ========================================================== */

    /**
     * 取得某分類底下全部 items（MVP）
     * - 主要用於後台 Master-Detail 右側 Items Table
     * - 建議依 sortOrder asc, itemCode asc 排序，確保顯示穩定
     */
    List<DictItemResp> findAllByCategoryId(UUID categoryId);

    /* ==========================================================
     * Update
     * ========================================================== */

    /**
     * 更新字典項目（Patch）
     * - 僅更新非 null 欄位
     * - 若更新 itemCode，需檢查在同 category 下唯一
     */
    DictItemResp update(UUID itemId, @Valid UpdateDictItemReq req);

    /* ==========================================================
     * Delete
     * ========================================================== */

    /**
     * 刪除字典項目（MVP：硬刪）
     * 正式環境建議改為「停用 enabled=false」或加引用檢查
     */
    void delete(UUID itemId);

    /* ==========================================================
     * Sort (Batch Patch)
     * ========================================================== */

    /**
     * 批次更新排序（拖曳排序 / 批次調整）
     * 規則（由 Service 實作）：
     * - orders 不可空
     * - orders 中所有 itemId 必須都屬於 req.categoryId（避免跨分類亂改）
     */
    void updateItemSort(@Valid SortPatchDictItemReq req);
}
