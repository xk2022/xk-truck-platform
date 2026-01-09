package com.xk.truck.adm.domain.repository;

import com.xk.truck.adm.domain.model.AdmDictCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository Interface : AdmDictCategoryRepository
 * Layer                : Domain Repository (Persistence)
 * Purpose              :
 * - 負責字典分類（Dictionary Category）的資料存取
 * <p>
 * Design Notes
 * - Repository 僅負責「資料查詢與儲存」
 * - 不放任何業務規則（唯一性意義、是否可刪除等）
 * - 查詢方法命名需對應實際使用場景，避免過度擴張
 * ===============================================================
 */
public interface AdmDictCategoryRepository extends JpaRepository<AdmDictCategory, UUID> {

    /* ==========================================================
     * Single Result Queries
     * ========================================================== */

    /**
     * 依 code 查詢單一 字典分類
     * - code 為業務唯一鍵
     */
    Optional<AdmDictCategory> findByCode(String code);

    /**
     * 檢查 code 是否已存在
     * - 用於建立/更新前的唯一性檢查
     */
    boolean existsByCode(String code);

    /* ==========================================================
     * Collection Queries (MVP)
     * ========================================================== */

    /**
     * 取得所有字典分類（依 code 排序）
     * - 提供後台 Master-Detail 左側使用
     * <p>
     * ⚠️ 注意：
     * - 若資料量成長，請避免直接使用 findAll()
     * - 建議改成分頁或條件查詢
     */
    @Query("select c from AdmDictCategory c order by c.code asc")
    List<AdmDictCategory> findAllOrderByCode();
}
