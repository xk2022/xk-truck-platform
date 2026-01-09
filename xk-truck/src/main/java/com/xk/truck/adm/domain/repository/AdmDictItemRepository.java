package com.xk.truck.adm.domain.repository;

import com.xk.truck.adm.domain.model.AdmDictItem;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Repository : AdmDictItemRepository
 * Purpose    :
 * - 字典項目資料存取
 * <p>
 * Key Rules
 * - (categoryUuid, itemCode) 唯一
 * ===============================================================
 */
public interface AdmDictItemRepository extends JpaRepository<AdmDictItem, UUID> {

    /* ----------------------------------------------------------
     * Exists / Uniqueness
     * ---------------------------------------------------------- */

    boolean existsByCategoryUuidAndItemCode(UUID categoryUuid, String itemCode);

    /* ----------------------------------------------------------
     * Query
     * ---------------------------------------------------------- */

    List<AdmDictItem> findAllByCategoryUuid(UUID categoryUuid, Sort sort);

    /* ----------------------------------------------------------
     * Sort helper
     * ---------------------------------------------------------- */

    /**
     * 取得下一個 sortOrder：max(sortOrder) + 1
     * 若該分類尚無 item，回傳 1
     */
    @Query("""
                select coalesce(max(i.sortOrder), 0) + 1
                from AdmDictItem i
                where i.categoryUuid = :categoryUuid
            """)
    Integer findNextSortOrder(@Param("categoryUuid") UUID categoryUuid);
}
