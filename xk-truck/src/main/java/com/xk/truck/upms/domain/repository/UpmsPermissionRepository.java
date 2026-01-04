package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsPermission;

import jakarta.persistence.QueryHint;

import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ===============================================================
 * Repository: UpmsPermissionRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 UpmsPermission （權限）實體的 CRUD 與常用查詢
 * Notes    :
 * - code 為權限唯一識別（如 UPMS_USER_READ）
 * - 權限通常由角色指派（RolePermission）
 * - Spring Data JPA 自動實作常規方法
 * - Repository 僅負責資料存取，不處理業務邏輯
 * - 查詢型方法可使用 EntityGraph 避免 N+1（若 permission 有關聯）
 * - 修改型 query 務必包在 Transaction 中
 * ===============================================================
 * <p>
 * 常見使用情境
 * 1) Permission 管理：CRUD、依 code/name 查詢
 * 2) Role 權限設定：依 systemCode 篩選、依群組/模組分類顯示
 * 3) Permission Tree：依 parentId 查子節點（如果你做階層式）
 * 4) 安全檢查：existsByCode / count / 引用數（搭配 RolePermissionRepo）
 * <p>
 * 欄位命名假設（請依你的 UpmsPermission 調整）
 * - uuid: UUID 主鍵
 * - code: String（唯一）
 * - name: String
 * - description: String?
 * - enabled: Boolean
 * - systemCode 或 system（ManyToOne UpmsSystem）二擇一
 * - type / resource / method / path（若你走 API Resource 權限）
 * - parent（ManyToOne UpmsPermission）+ children（OneToMany）若你做 Tree
 * ===============================================================
 */
@Repository
public interface UpmsPermissionRepository
        extends JpaRepository<UpmsPermission, UUID>, JpaSpecificationExecutor<UpmsPermission> {

    // ===============================================================
    // 1️⃣ Basic lookups（最安全、最常用）
    // ===============================================================

    /**
     * 以 code 代碼查詢 Permission（通常用於後端校驗 / 建立關聯前查）
     */
    Optional<UpmsPermission> findByCode(String code);

    /**
     * 是否已存在相同權限代碼 code（建立/更新時做唯一性檢查）
     */
    boolean existsByCode(String code);

    /**
     * 排除自己 id 的 code 唯一檢查（更新時常用）
     */
    @Query("""
        select (count(p) > 0)
        from UpmsPermission p
        where p.code = :code
          and p.uuid <> :id
    """)
    boolean existsByCodeAndUuidNot(@Param("code") String code, @Param("id") UUID id);

    /**
     * 統計啟用/停用數
     */
    long countByEnabled(Boolean enabled);

    /**
     * 依 code 前綴統計（例如：UPMS_% / FMS_%）
     * - 若你有慣例用 prefix 分模組，這個很實用
     */
    @Query("""
        select count(p)
        from UpmsPermission p
        where p.code like concat(:prefix, '%')
    """)
    long countByCodePrefix(@Param("prefix") String prefix);

    // ===============================================================
    // 2️⃣ 常用清單查詢（不 EntityGraph、不 join，輕量）
    // ===============================================================

    /**
     * 下拉選單用：只拿啟用的 permissions（依 code 排序）
     */
    @Query("""
        select p
        from UpmsPermission p
        where p.enabled = true
        order by p.code asc
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsPermission> findAllEnabled();

    /**
     * 依 codes 批次查詢（常用於：RolePermission 覆蓋式指派時先查 permission 集）
     */
    @Query("""
        select p
        from UpmsPermission p
        where p.code in :codes
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsPermission> findAllByCodeIn(@Param("codes") Collection<String> codes);

    /**
     * 依 uuids 批次查詢
     */
    @Query("""
        select p
        from UpmsPermission p
        where p.uuid in :ids
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsPermission> findAllByUuidIn(@Param("ids") Collection<UUID> ids);

    // ===============================================================
    // 3️⃣ EntityGraph / Fetch optimization（避免 N+1）
    // ===============================================================
    // 若你的 UpmsPermission 有 system / parent / children 等關聯，才需要用

    /**
     * 取得單筆 Permission（同時抓關聯）
     * - 視你的 entity 而定：system / parent 這種 ManyToOne 抓了也不重
     *
     * ⚠ attributePaths 必須與你的欄位名稱一致
     *   - 若你沒有 system/parent，請把 attributePaths 拿掉或留空
     */
    @EntityGraph(attributePaths = {
            "system",
            "parent"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("select p from UpmsPermission p where p.uuid = :id")
    Optional<UpmsPermission> findDetailByUuid(@Param("id") UUID id);

    /**
     * 分頁查詢（同時抓 system / parent）
     * - Page + EntityGraph 若遇到供應商差異，再改用 DTO projection（見下方第4段）
     */
    @EntityGraph(attributePaths = {
            "system",
            "parent"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<UpmsPermission> findAll(Pageable pageable);

    boolean existsBySystemCodeAndResourceCodeAndActionCode(String systemCode, String resourceCode, String actionCode);

    // ===============================================================
    // 4️⃣ DTO / Projection Query（最穩的列表頁）
    // ===============================================================

    /**
     * Projection：Permission 列表列資料（避免拉整個 entity + 關聯）
     * - 你可以用於 /api/upms/permissions page list
     * - 欄位依你的 entity 自由加減
     */
    interface PermissionRow {
        UUID getId();
        String getCode();
        String getName();
        Boolean getEnabled();
        String getDescription();

        // Optional（存在才會有）
        String getSystemCode();     // 若是 p.system.code
        UUID getParentId();         // 若是 p.parent.uuid

        LocalDateTime getCreatedTime();
        LocalDateTime getUpdatedTime();
    }

    /**
     * 分頁列表（含 keyword 搜尋：code/name/systemCode）
     * - 依你的欄位調整 where 子句
     */
//    @Query("""
//        select
//            p.uuid as id,
//            p.code as code,
//            p.name as name,
//            p.enabled as enabled,
//            p.description as description,
//            s.code as systemCode,
//            parent.uuid as parentId,
//            p.createdTime as createdTime,
//            p.updatedTime as updatedTime
//        from UpmsPermission p
//        left join p.system s
//        left join p.parent parent
//        where (:keyword is null
//               or lower(p.code) like lower(concat('%', :keyword, '%'))
//               or lower(p.name) like lower(concat('%', :keyword, '%'))
//               or (s.code is not null and lower(s.code) like lower(concat('%', :keyword, '%'))))
//    """)
//    @QueryHints({
//            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
//    })
//    Page<PermissionRow> pagePermissionRows(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );

    /**
     * 依 systemCode 分頁（非常常用：不同模組分頁顯示）
     */
//    @Query("""
//        select
//            p.uuid as id,
//            p.code as code,
//            p.name as name,
//            p.enabled as enabled,
//            p.description as description,
//            s.code as systemCode,
//            parent.uuid as parentId,
//            p.createdTime as createdTime,
//            p.updatedTime as updatedTime
//        from UpmsPermission p
//        join p.system s
//        left join p.parent parent
//        where s.code = :systemCode
//          and (:keyword is null
//               or lower(p.code) like lower(concat('%', :keyword, '%'))
//               or lower(p.name) like lower(concat('%', :keyword, '%')))
//    """)
//    @QueryHints({
//            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
//    })
//    Page<PermissionRow> pagePermissionRowsBySystemCode(
//            @Param("systemCode") String systemCode,
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );

    // ===============================================================
    // 5️⃣ Tree / Hierarchy（如果你有 parent/children）
    // ===============================================================

    /**
     * 查 root permissions（parent is null）
     * - 做 tree 時很常用
     */
//    @Query("""
//        select p
//        from UpmsPermission p
//        where p.parent is null
//        order by p.sortOrder asc nulls last, p.code asc
//    """)
//    @QueryHints({
//            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
//    })
//    List<UpmsPermission> findRoots();

    /**
     * 依 parentUuid 查 children
     */
//    @Query("""
//        select p
//        from UpmsPermission p
//        where p.parent.uuid = :parentId
//        order by p.sortOrder asc nulls last, p.code asc
//    """)
//    @QueryHints({
//            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
//    })
//    List<UpmsPermission> findChildrenByParentUuid(@Param("parentId") UUID parentId);

    // ===============================================================
    // 6️⃣ Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================

    /**
     * 更新啟用狀態（避免整個 entity 拉出來改）
     * @return 影響筆數（1=成功, 0=查無此人）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsPermission p
        set p.enabled = :enabled
        where p.uuid = :id
    """)
    int updateEnabled(
            @Param("id") UUID id,
            @Param("enabled") Boolean enabled
    );

    /**
     * 更新名稱/描述（簡單 bulk update 範例）
     * - 若你有 version 欄位（@Version），bulk update 不會觸發 version + entity listener
     *   → 這是 bulk update 的特性，請評估是否需要改回 save(entity)
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsPermission p
        set p.name = :name,
            p.description = :description,
            p.updatedTime = :time
        where p.uuid = :id
    """)
    int updateBasicInfo(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("time") LocalDateTime time
    );

    /**
     * 更新最後異動時間（範例）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsPermission p
        set p.updatedTime = :time
        where p.uuid = :id
    """)
    int updateUpdatedTime(
            @Param("id") UUID id,
            @Param("time") LocalDateTime time
    );

    /**
     * 批次刪除（依 ids）
     * - ⚠ 刪除 permission 前，務必先清掉 role_permission 關聯（避免 FK constraint）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from UpmsPermission p
        where p.uuid in :ids
    """)
    int deleteByUuidIn(@Param("ids") Collection<UUID> ids);

    /**
     * 批次停用（依 ids）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsPermission p
        set p.enabled = false
        where p.uuid in :ids
    """)
    int disableByUuidIn(@Param("ids") Collection<UUID> ids);

    /**
     * 依 permission code 清單，查出對應的 permission UUID
     *
     * 用途：
     * - Role → Permission replace / assign
     * - 避免載入整個 Permission entity（效能好、低耦合）
     *
     * ⚠️ 排雷：
     * - 回傳 UUID，不要回傳 entity
     * - codes 可能重複，service 端再 distinct
     */
    @Query("""
        select p.uuid
        from UpmsPermission p
        where p.code in :codes
    """)
    List<UUID> findUuidsByCodes(@Param("codes") Collection<String> codes);


    boolean existsByCodeAndDeletedAtIsNull(String code);
}
