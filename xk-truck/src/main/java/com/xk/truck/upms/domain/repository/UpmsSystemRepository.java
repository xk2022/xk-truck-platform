package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsSystem;

import jakarta.persistence.QueryHint;

import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: UpmsSystemRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 UpmsSystem 實體的 CRUD 與常用查詢
 * Notes    :
 * - Spring Data JPA 自動實作常規方法
 * - Repository 僅負責資料存取，不處理業務邏輯
 * - 查詢型方法可使用 EntityGraph 避免 N+1（例如要抓 permissions）
 * - 修改型 query 務必包在 Transaction 中
 * ===============================================================
 *
 * 常見使用情境
 * 1) UPMS System 管理：系統清單/排序/啟用狀態（UPMS、ADM、FMS、TOM…）
 * 2) 權限資源依系統分組：Permission 依 systemId/systemCode 查詢
 * 3) UI Sidebar/Router：依 enabled + sortOrder 輸出可用系統清單
 *
 * 欄位命名假設（請依你的 UpmsSystem 調整）
 * - uuid: UUID 主鍵
 * - code: String（唯一）
 * - name: String
 * - description: String?
 * - enabled: Boolean
 * - sortOrder: Integer?
 * - remark: String?
 * - permissions: OneToMany（可選，若你做 system -> permissions）
 * ===============================================================
 */
@Repository
public interface UpmsSystemRepository
        extends JpaRepository<UpmsSystem, UUID>,
        JpaSpecificationExecutor<UpmsSystem> {

    // ===============================================================
    // Basic lookups（最安全、最常用）
    // ===============================================================

    /**
     * 以 code 查詢 System（建立關聯/校驗時常用）
     */
    Optional<UpmsSystem> findByCode(String code);

    /**
     * 是否存在相同 code（建立/更新時唯一性檢查）
     */
    boolean existsByCode(String code);

    /**
     * 排除自己 id 的 code 唯一檢查（更新時常用）
     */
    @Query("""
        select (count(s) > 0)
        from UpmsSystem s
        where s.code = :code
          and s.uuid <> :id
    """)
    boolean existsByCodeAndUuidNot(@Param("code") String code, @Param("id") UUID id);

    /**
     * 統計啟用/停用
     */
    long countByEnabled(Boolean enabled);

    // ===============================================================
    // 2Read-only list（前台常用：Sidebar/下拉）
    // ===============================================================

    /**
     * 取得所有啟用的 System（依 sortOrder > code 排序）
     * - Sidebar/系統切換下拉通常直接用這支
     */
    @Query("""
        select s
        from UpmsSystem s
        where s.enabled = true
        order by s.sortOrder asc nulls last, s.code asc
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsSystem> findAllEnabledOrderBySort();

    /**
     * 依 codes 批次查詢（例如：一次取 UPMS/ADM/FMS）
     */
    @Query("""
        select s
        from UpmsSystem s
        where s.code in :codes
        order by s.sortOrder asc nulls last, s.code asc
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsSystem> findAllByCodeIn(@Param("codes") Collection<String> codes);

    // ===============================================================
    // 3️⃣ EntityGraph / Fetch optimization（避免 N+1）
    // ===============================================================
    // 如果 UpmsSystem 有 permissions 關聯（system.permissions），才建議用

    /**
     * 取得單筆 System（同時抓 permissions）
     * - ⚠ attributePaths 必須與你的欄位名稱一致
     * - 若你尚未建立 permissions 關聯，請把 attributePaths 改為空或移除
     */
    @EntityGraph(attributePaths = {
            "permissions"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("select s from UpmsSystem s where s.uuid = :id")
    Optional<UpmsSystem> findDetailByUuid(@Param("id") UUID id);

    /**
     * 分頁查詢（同時抓 permissions）
     * - Page + EntityGraph 若遇到供應商問題，改 DTO projection 最穩（見第4段）
     */
    @EntityGraph(attributePaths = {
            "permissions"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<UpmsSystem> findAll(Pageable pageable);

    // ===============================================================
    // 4️⃣ DTO / Projection Query（列表最穩）
    // ===============================================================

    /**
     * Projection：System 列表列資料（避免拉整個 entity）
     */
    interface SystemRow {
        UUID getId();
        String getCode();
        String getName();
        Boolean getEnabled();
        String getDescription();
        Integer getSortOrder();
        String getRemark();
        LocalDateTime getCreatedTime();
        LocalDateTime getUpdatedTime();
    }

    /**
     * 分頁列表（含 keyword：code/name）
     */
    @Query("""
        select
            s.uuid as id,
            s.code as code,
            s.name as name,
            s.enabled as enabled,
            s.description as description,
            s.sortOrder as sortOrder,
            s.remark as remark,
            s.createdTime as createdTime,
            s.updatedTime as updatedTime
        from UpmsSystem s
        where (:keyword is null
               or lower(s.code) like lower(concat('%', :keyword, '%'))
               or lower(s.name) like lower(concat('%', :keyword, '%')))
        order by s.sortOrder asc nulls last, s.code asc
    """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<SystemRow> pageSystemRows(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ===============================================================
    // 5️⃣ Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================

    /**
     * 更新啟用狀態（避免整個 entity 拉出來改）
     * @return 影響筆數（1=成功, 0=查無此人）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsSystem s
        set s.enabled = :enabled
        where s.uuid = :id
    """)
    int updateEnabled(
            @Param("id") UUID id,
            @Param("enabled") Boolean enabled
    );

    /**
     * 更新排序（拖拉排序/調整功能常用）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsSystem s
        set s.sortOrder = :sortOrder,
            s.updatedTime = :time
        where s.uuid = :id
    """)
    int updateSortOrder(
            @Param("id") UUID id,
            @Param("sortOrder") Integer sortOrder,
            @Param("time") LocalDateTime time
    );

    /**
     * 更新基本資訊（name/description/remark）
     * - 若你有 @Version 或 Auditing Listener，bulk update 不會觸發，這是 bulk 特性
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsSystem s
        set s.name = :name,
            s.description = :description,
            s.remark = :remark,
            s.updatedTime = :time
        where s.uuid = :id
    """)
    int updateBasicInfo(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("remark") String remark,
            @Param("time") LocalDateTime time
    );

    /**
     * 更新最後異動時間（範例）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsSystem s
        set s.updatedTime = :time
        where s.uuid = :id
    """)
    int updateUpdatedTime(
            @Param("id") UUID id,
            @Param("time") LocalDateTime time
    );

    /**
     * 批次停用（依 ids）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsSystem s
        set s.enabled = false
        where s.uuid in :ids
    """)
    int disableByUuidIn(@Param("ids") Collection<UUID> ids);

    /**
     * 批次刪除（依 ids）
     * - ⚠ 若 System 仍被 Permission 參照（FK），先處理 Permission（或設計為 cascade / restrict）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from UpmsSystem s
        where s.uuid in :ids
    """)
    int deleteByUuidIn(@Param("ids") Collection<UUID> ids);
}
