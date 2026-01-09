package com.xk.truck.adm.domain.service;

import com.xk.truck.adm.controller.api.dto.CreateDictCategoryReq;
import com.xk.truck.adm.controller.api.dto.DictCategoryResp;
import com.xk.truck.adm.controller.api.dto.UpdateDictCategoryReq;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * UseCase Interface : AdmDictCategoryService
 * Layer             : Domain Service
 * Purpose           :
 * - 定義「字典分類（Dictionary Category）」核心用例（Use Cases）
 * - 僅描述「能做什麼」，不關心資料來源（DB / Cache / Remote）
 * <p>
 * Design Notes
 * - Controller 僅依賴此介面，不直接依賴 Repository
 * - 業務規則（唯一性、狀態限制、刪除策略）由 Impl 統一處理
 * - 回傳使用 DTO（Resp），避免 Entity 外洩到 Controller/UI
 * <p>
 * Typical Rules（由 Impl 實作）
 * - code 必須唯一（create）
 * - enabled 預設 true（create）
 * - Patch semantics：只更新非 null 欄位（update）
 * - 建議：正式環境「不允許更新 code」（避免前端/其他系統 hardcode 失效）
 * ===============================================================
 */
public interface AdmDictCategoryService {

    // ==========================================================
    // Create
    // ==========================================================

    /**
     * 建立字典分類
     * <p>
     * Business Rules:
     * - code 必須唯一
     * - enabled 預設 true
     *
     * @param req 建立請求資料
     * @return 建立完成的字典分類
     */
    DictCategoryResp create(CreateDictCategoryReq req);

    // ==========================================================
    // Read
    // ==========================================================

    /**
     * 依 code 查詢單一 字典分類
     * <p>
     * Use Cases:
     * - 後台管理：編輯/檢視
     * - 其他模組：以 code 取得分類資訊（例如配置、校驗）
     *
     * @param code 字典分類代碼（穩定識別鍵）
     * @return 字典分類資訊
     */
    DictCategoryResp findByCode(String code);

    /**
     * 查詢所有字典分類（MVP）
     * <p>
     * Use Cases:
     * - 後台 Master-Detail 左側清單一次載入
     * <p>
     * Notes:
     * - 目前假設資料量不大
     * - 實作端建議固定排序（例如 code asc），確保 UI 穩定
     * <p>
     * Weakness:
     * - 若分類數量上升到 300~500+，一次載入可能變慢
     * 建議改成 pageable/query（keyword/enabled/page/size/sort）
     *
     * @return 字典分類清單（建議依 code 排序）
     */
    List<DictCategoryResp> findAll();

    // v2（需要時再打開）
    // Page<DictCategoryListResp> pageForList(DictCategoryQuery query, Pageable pageable);

    // ==========================================================
    // Update
    // ==========================================================

    /**
     * 更新字典分類（Patch semantics）
     * <p>
     * Business Rules:
     * - 僅更新非 null 欄位
     * - 若允許更新 code，需檢查唯一性
     * <p>
     * Recommendation:
     * - 正式環境建議「禁止更新 code」
     * 理由：避免前端/其他系統 hardcode 的 code 失效
     *
     * @param id  字典分類 UUID
     * @param req 更新請求資料（可為部分欄位）
     * @return 更新後的字典分類
     */
    DictCategoryResp update(UUID id, UpdateDictCategoryReq req);

    // ==========================================================
    // Delete
    // ==========================================================

    /**
     * 刪除字典分類（MVP：硬刪）
     * <p>
     * ⚠️ Weakness / Risks:
     * - 若分類底下已有 items 或被業務資料引用：
     * 1) 可能觸發 FK constraint
     * 2) 或造成歷史資料顯示缺失（label 顯示 '-'）
     * <p>
     * Recommendation:
     * - 正式環境建議改為「停用 enabled=false」
     * - 或刪除前做引用檢查（由 Impl 決定策略）
     *
     * @param id 字典分類 UUID
     */
    void delete(UUID id);
}
