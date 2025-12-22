package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsRole;
import com.xk.truck.upms.domain.model.UpmsRolePermission;

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
 * Repository: UpmsRolePermissionRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 UpmsRolePermission（角色-權限 中介表）CRUD 與常用查詢
 * Notes    :
 * - 對應資料表：upms_role_permission
 * - 一個角色可綁定多個權限
 * - Spring Data JPA 自動實作常規方法
 * - Repository 僅負責資料存取，不處理業務邏輯
 * - 查詢型方法可使用 EntityGraph 避免 N+1（但關聯表通常很輕量）
 * - 修改型 query 務必包在 Transaction 中
 * - 中介表最常見的需求：差集同步（toAdd / toRemove）
 * - 這裡提供「只查 UUID」的 query，避免把 Role/Permission entity 拉出來造成耦合與 N+1
 * - 修改型 query 務必包在 Transaction 中（@Transactional + @Modifying）
 * ===============================================================
 * <p>
 * 常見使用情境
 * 1) 角色詳情：查某角色目前綁了哪些 permissions
 * 2) 權限回填：角色權限設定頁（勾選哪些 permission）
 * 3) 覆蓋式更新：先刪掉角色原本關聯，再批次插入新關聯
 * 4) 快速檢查：某角色是否含某 permission
 * <p>
 * 地雷排除（很重要）
 * - 這張表通常會有唯一鍵（role_uuid, permission_uuid）
 * → repository 提供 exists/find/delete 來支援「覆蓋式指派」而不踩重複 key
 * - 不建議用 saveAll 前先逐筆 exists（N+1），應該：
 * A) deleteByRoleUuid(roleId) 然後 saveAll(newLinks)（最常見）
 * B) 或用 bulk insert（需要 native / custom repo）
 * ===============================================================
 */
@Repository
public interface UpmsRolePermissionRepository
        extends JpaRepository<UpmsRolePermission, UUID>,
        JpaSpecificationExecutor<UpmsRolePermission> {

    // ===============================================================
    // Basic lookups（最安全、最常用）
    // ===============================================================

    /**
     * 依 roleUuid + permissionUuid 查關聯（常用於避免重複綁定 / debug）
     */
    @Query("""
                select rp
                from UpmsRolePermission rp
                where rp.role.uuid = :roleId
                  and rp.permission.uuid = :permId
            """)
    Optional<UpmsRolePermission> findByRoleUuidAndPermissionUuid(
            @Param("roleId") UUID roleId,
            @Param("permId") UUID permId
    );

    /**
     * 是否已存在關聯（避免重複 insert 觸發 unique constraint）
     * <p>
     * ⚠ 這裡走「以 FK UUID」判斷，不碰 entity equals/hashCode，避免 Set 坑。
     */
    boolean existsByRoleUuidAndPermissionUuid(UUID roleUuid, UUID permissionUuid);

    /**
     * 依 roleUuid 統計綁定數量（列表頁顯示 permissionCount / 監控）
     */
    @Query("""
                select count(rp)
                from UpmsRolePermission rp
                where rp.role.uuid = :roleId
            """)
    long countByRoleUuid(@Param("roleId") UUID roleId);

    /**
     * 依 permissionUuid 統計被多少角色引用（刪權限前檢查）
     */
    @Query("""
                select count(rp)
                from UpmsRolePermission rp
                where rp.permission.uuid = :permId
            """)
    long countByPermissionUuid(@Param("permId") UUID permId);

    /**
     * 查某角色的所有關聯（不抓 permission 本體）
     * - 適合：只要拿 permissionUuid 清單
     */
    @Query("""
                select rp
                from UpmsRolePermission rp
                where rp.role.uuid = :roleId
                order by rp.createdTime asc
            """)
    List<UpmsRolePermission> findAllByRoleUuid(@Param("roleId") UUID roleId);

    /**
     * 取得某角色目前擁有的 Permission UUIDs
     * <p>
     * 用途：
     * - replacePermissions() 差集同步
     * - 不拉出 Permission entity，避免 lazy 連鎖載入
     * <p>
     * 回傳 Set：
     * - 使用者端直接做集合差集（toAdd/toRemove）
     * - 通常 UUID 唯一，不需要 distinct，但仍可保險加 distinct
     */
    @Query("""
                select distinct rp.permissionUuid
                from UpmsRolePermission rp
                where rp.roleUuid = :roleUuid
            """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Set<UUID> findPermissionUuidsByRoleUuid(@Param("roleUuid") UUID roleUuid);

    /**
     * 只查某角色綁定的 permission codes（若 permission.code 是常用顯示欄位）
     */
    @Query("""
                select rp.permission.code
                from UpmsRolePermission rp
                where rp.role.uuid = :roleId
                order by rp.permission.code asc
            """)
    List<String> findPermissionCodesByRoleUuid(@Param("roleId") UUID roleId);

    /**
     * 批次查詢：多個角色一次取回所有關聯（可用來組 map<roleId, permIds>）
     */
    @Query("""
                select rp
                from UpmsRolePermission rp
                where rp.role.uuid in :roleIds
            """)
    List<UpmsRolePermission> findAllByRoleUuidIn(@Param("roleIds") Collection<UUID> roleIds);

    // ===============================================================
    // EntityGraph / Fetch optimization（避免 N+1）
    // ===============================================================
    // 關聯表通常很小，但「需要 permission 詳細資訊」時會用到

    /**
     * 查某角色的關聯（同時抓 permission）
     * - 適合：角色權限設定頁回填已選 + 顯示 permission 名稱/描述
     * <p>
     * ⚠️ attributePaths 需對應 UpmsRolePermission 的欄位名稱：permission
     */
    @EntityGraph(attributePaths = {"permission"})
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("""
                select rp
                from UpmsRolePermission rp
                where rp.role.uuid = :roleId
                order by rp.permission.code asc
            """)
    List<UpmsRolePermission> findAllByRoleUuidWithPermission(@Param("roleId") UUID roleId);

    /**
     * 查某權限被哪些角色使用（同時抓 role）
     * - 適合：Permission detail 顯示「被哪些角色引用」
     */
    @EntityGraph(attributePaths = {"role"})
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("""
                select rp
                from UpmsRolePermission rp
                where rp.permission.uuid = :permId
                order by rp.role.code asc
            """)
    List<UpmsRolePermission> findAllByPermissionUuidWithRole(@Param("permId") UUID permId);

    /**
     * 分頁查詢（同時抓 role + permission）
     * - 適合：後台稽核/查詢「誰綁了誰」
     * - 若你覺得太重，可以把 EntityGraph 拿掉
     */
    @EntityGraph(attributePaths = {"role", "permission"})
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<UpmsRolePermission> findAll(Pageable pageable);

    // ===============================================================
    // DTO Query（更穩、更輕量的列表/報表）
    // ===============================================================

    interface RolePermissionRow {
        UUID getId();

        UUID getRoleId();

        String getRoleCode();

        UUID getPermissionId();

        String getPermissionCode();

        LocalDateTime getCreatedTime();

        LocalDateTime getUpdatedTime();
    }

    /**
     * 角色 ↔ 權限 列表（projection）
     * - 可做後台查詢/匯出
     * - 可帶 keyword 搜尋 roleCode/permissionCode
     */
    @Query("""
                select
                    rp.uuid as id,
                    r.uuid as roleId,
                    r.code as roleCode,
                    p.uuid as permissionId,
                    p.code as permissionCode,
                    rp.createdTime as createdTime,
                    rp.updatedTime as updatedTime
                from UpmsRolePermission rp
                join rp.role r
                join rp.permission p
                where (:keyword is null
                       or lower(r.code) like lower(concat('%', :keyword, '%'))
                       or lower(p.code) like lower(concat('%', :keyword, '%')))
            """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<RolePermissionRow> pageRolePermissionRows(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ===============================================================
    // Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================

    /**
     * 清空某角色所有權限關聯
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.roleUuid = :roleUuid
            """)
    int deleteByRoleUuid(@Param("roleUuid") UUID roleUuid);

    /**
     * 刪除某權限全部角色關聯（刪 permission 前常用）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.permission.uuid = :permId
            """)
    int deleteByPermissionUuid(@Param("permId") UUID permId);


    /**
     * 刪除單筆關聯（角色-權限）
     * - 用於 removePermission(roleUuid, permissionUuid)
     *
     * @return 影響筆數（1=成功刪除, 0=原本就不存在）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.roleUuid = :roleUuid
                  and rp.permissionUuid = :permissionUuid
            """)
    int deleteByRoleUuidAndPermissionUuid(
            @Param("roleUuid") UUID roleUuid,
            @Param("permissionUuid") UUID permissionUuid
    );

    /**
     * 批次刪除：多個 role 一次清掉（例如：移除多個角色的所有權限）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.role.uuid in :roleIds
            """)
    int deleteByRoleUuidIn(@Param("roleIds") Collection<UUID> roleIds);

    /**
     * 批次刪除：多個 permission 一次清掉（例如：清理測試 permissions）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.permission.uuid in :permIds
            """)
    int deleteByPermissionUuidIn(@Param("permIds") Collection<UUID> permIds);

    /**
     * 範例：更新 updatedTime（通常不需要，但給你 ops 手段）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsRolePermission rp
                set rp.updatedTime = :time
                where rp.uuid = :id
            """)
    int updateUpdatedTime(
            @Param("id") UUID id,
            @Param("time") LocalDateTime time
    );

    /**
     * ✅ 你點名缺漏的重點：bulk delete（roleUuid + permissionUuid IN）
     * <p>
     * 用途：
     * - replacePermissions() 內的 toRemove 批次刪除
     * <p>
     * ⚠ JPQL 的 IN (:ids)：
     * - ids 為空集合時，有些 provider 會報錯，因此 Service 層必須先判斷 isEmpty()
     * - 你在 Service 已經做了 if (!toRemove.isEmpty())，這樣是正確的
     *
     * @return removed 筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsRolePermission rp
                where rp.roleUuid = :roleUuid
                  and rp.permissionUuid in :permissionUuids
            """)
    int deleteByRoleUuidAndPermissionUuidIn(
            @Param("roleUuid") UUID roleUuid,
            @Param("permissionUuids") Collection<UUID> permissionUuids
    );


    @Query("""
        select rp.permission.uuid
        from UpmsRolePermission rp
        where rp.role in :roles
    """)
    Set<UUID> findPermissionUuidsByRoleIn(@Param("roles") Collection<UpmsRole> roles);

    List<UpmsRolePermission> findByRoleUuidIn(Collection<UUID> roleUuids);
}
