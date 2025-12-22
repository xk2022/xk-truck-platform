package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsRole;

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
 * Service Class : UpmsRoleRepository
 * Layer         : Domain DAO (JPA Repository)
 * Purpose       : 提供 UpmsRole （角色）實體的 CRUD 與常用查詢
 * Notes         :
 * - roleCode 建議全大寫保存（Service normalize）
 * - 角色以 code 唯一識別（如：ADMIN, DISPATCH, DRIVER）
 * - 可用於角色管理與授權模組
 * - Spring Data JPA 自動實作常規方法
 * - Repository 僅負責資料存取，不處理業務邏輯
 * - 查詢型方法可使用 EntityGraph 避免 N+1
 * - 修改型 query 務必包在 Transaction 中
 * ===============================================================
 * <p>
 * 常見使用情境
 * 1) 登入授權：依 roleCodes 批次查角色（不抓 permissions，避免過重）
 * 2) 管理後台：角色列表（可抓 permissionCodes 或 permissionCount）
 * 3) 角色詳情：抓 role + rolePermissions + permissions（一次載入避免 N+1）
 * <p>
 * ⚠重要提醒（地雷排除）
 * - Page + JOIN FETCH 不能共存（Hibernate/JPA 規則），要用 EntityGraph 或 DTO query
 * - 若 permissions 很大，列表頁不要抓 permissions（只抓 count / codes）
 * ===============================================================
 */
@Repository
public interface UpmsRoleRepository
        extends JpaRepository<UpmsRole, UUID>,
        JpaSpecificationExecutor<UpmsRole> {

    // 給下拉選單 / options 用（只要啟用中的）
//    List<Role> findAllByEnabledTrueOrderByCodeAsc();

    // ===============================================================
    // Basic lookups（最安全、最常用）
    // ===============================================================
    // 原則：
    // - 單一條件
    // - 不 JOIN
    // - 不 EntityGraph
    // - 回 Optional / boolean / count

    /**
     * 依 角色代碼（唯一） 查詢
     */
    Optional<UpmsRole> findByCode(String code);

    /**
     * 是否已存在<相同角色代碼>
     */
    boolean existsByCode(String code);

    /**
     * 依 enabled 統計
     */
    long countByEnabled(Boolean enabled);

    /**
     * 依多個角色代碼(code)查詢角色集合（常用於：JWT / 登入授權載入 user roles）
     * * - 不抓 permissions，避免負載過重
     */
    List<UpmsRole> findByCodeIn(Collection<String> codes);

    /**
     * 依多個角色 uuid 查詢角色清單
     */
    List<UpmsRole> findByUuidIn(Collection<UUID> uuids);

    /**
     * 依 code 模糊查詢（後台搜尋）
     * - 注意：這種 like 查詢較慢，建議搭配 index / 規範化（或走 Spec）
     */
    Page<UpmsRole> findByCodeContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * 依 name 模糊查詢（後台搜尋）
     */
    Page<UpmsRole> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // ===============================================================
    // EntityGraph / Fetch optimization（避免 N+1）
    // ===============================================================
    // 原則：
    // - 只為了解決 N+1
    // - 不放業務邏輯
    // - Page + EntityGraph 通常 OK；若遇到 provider 差異，改 DTO query 最穩

    /**
     * 取得角色（只抓基本資料，ReadOnly）
     * - 用於：detail 之前先確認存在、或只需要 role 本體
     */
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Optional<UpmsRole> findByUuid(UUID uuid);

    /**
     * 角色詳情：抓 role + rolePermissions（關聯表）
     * - 適合：顯示「已綁定哪些 permission」但 permission 本體不一定要一起抓
     * - attributePaths 需對應 UpmsRole 裡的關聯欄位名稱（例如：rolePermissions）
     */
    @EntityGraph(attributePaths = {
            "rolePermissions"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("select r from UpmsRole r where r.uuid = :id")
    Optional<UpmsRole> findDetailWithRolePermissions(@Param("id") UUID id);

    /**
     * 角色詳情：抓 role + rolePermissions + permission（一次載入完整授權資訊）
     * - 適合：角色權限設定頁（勾選清單、回填已選）
     * - 這裡抓到 permission 本體，避免後續 N+1
     * <p>
     * ⚠️ attributePaths 寫法：rolePermissions.permission
     * 前提：
     * - UpmsRole.rolePermissions
     * - UpmsRolePermission.permission
     */
    @EntityGraph(attributePaths = {
            "rolePermissions",
            "rolePermissions.permission"
    })
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("select r from UpmsRole r where r.uuid = :id")
    Optional<UpmsRole> findDetailWithPermissions(@Param("id") UUID id);

    /**
     * 列表頁：抓 role（可選擇是否抓 rolePermissions）
     * - 一般列表不建議抓 permissions，最多抓 rolePermissions（用於顯示數量）
     * - 若 permissions 很大，建議改 DTO query 直接回傳 count
     */
//    @EntityGraph(attributePaths = {
//            // "rolePermissions"
//    })
//    @QueryHints({
//            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
//    })
//    Page<UpmsRole> findAllWithDetail(Pageable pageable);

    // ===============================================================
    // 3️⃣ DTO Query（列表頁最穩：回傳 permissionCount / codes）
    // ===============================================================
    // 用途：
    // - 你想要列表顯示「權限數量」但又不想抓完整 permission
    // - 避免 Page + fetch join 的限制
    //
    // ⚠️ 這裡示範以 interface projection 回傳
    // 你也可以改成 record / class DTO
    // ===============================================================

    interface RoleWithPermissionCount {
        UUID getId();

        String getCode();

        String getName();

        String getDescription();

        Boolean getEnabled();

        Long getPermissionCount();

        LocalDateTime getCreatedTime();

        LocalDateTime getUpdatedTime();
    }

    /**
     * 角色列表（帶 permissionCount）
     * - 不會載入 permissions 本體
     * - Page 安全穩定
     * <p>
     * ⚠️ 欄位名稱需對應 UpmsRole / BaseEntity 欄位
     * - uuid / code / name / description / enabled / createdTime / updatedTime
     * ⚠️ rp 需要對應 UpmsRolePermission 的 mappedBy 關聯名稱
     * - 若 UpmsRolePermission 裡的 role 欄位名稱不同，請調整 join 條件
     */
//    @Query(
//            value = """
//    select
//        r.uuid as id,
//        r.code as code,
//        r.name as name,
//        r.description as description,
//        r.enabled as enabled,
//        count(rp) as permissionCount,
//        r.createdTime as createdTime,
//        r.updatedTime as updatedTime
//    from UpmsRole r
//    left join r.rolePermissions rp
//    where (:keyword is null
//           or lower(r.code) like lower(concat('%', :keyword, '%'))
//           or lower(r.name) like lower(concat('%', :keyword, '%')))
//    group by r.uuid, r.code, r.name, r.description, r.enabled, r.createdTime, r.updatedTime
//  """,
//            countQuery = """
//    select count(r)
//    from UpmsRole r
//    where (:keyword is null
//           or lower(r.code) like lower(concat('%', :keyword, '%'))
//           or lower(r.name) like lower(concat('%', :keyword, '%')))
//  """
//    )
//    Page<RoleWithPermissionCount> pageRoleWithPermissionCount(
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );

    // ===============================================================
    // 4️⃣ Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================
    // 原則：
    // - 一定 @Transactional
    // - 一定 @Modifying(clearAutomatically = true, flushAutomatically = true)
    // - 回傳 int（影響筆數）

    /**
     * 更新啟用狀態（不拉整個 entity 出來改）
     *
     * @return 影響筆數（1=成功, 0=查無資料）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsRole r
                set r.enabled = :enabled
                where r.uuid = :id
            """)
    int updateEnabled(
            @Param("id") UUID id,
            @Param("enabled") Boolean enabled
    );

    /**
     * 更新名稱 / 描述（常用於後台編輯）
     * - code 通常不建議更新（若你允許，請另外做 updateCode 並加上唯一性檢查）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsRole r
                set r.name = :name,
                    r.description = :description,
                    r.updatedTime = :updatedTime
                where r.uuid = :id
            """)
    int updateBasicInfo(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("updatedTime") LocalDateTime updatedTime
    );

    /**
     * 僅更新 updatedTime（範例）
     * - 有些情境你只想觸發「最後更新時間」
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsRole r
                set r.updatedTime = :time
                where r.uuid = :id
            """)
    int updateUpdatedTime(
            @Param("id") UUID id,
            @Param("time") LocalDateTime time
    );

    /**
     * 批次停用（例如：清理測試資料）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsRole r
                set r.enabled = false,
                    r.updatedTime = :time
                where r.uuid in :ids
            """)
    int disableByIds(
            @Param("ids") Collection<UUID> ids,
            @Param("time") LocalDateTime time
    );

    // ===============================================================
    // Batch lookup（你缺的）
    // ===============================================================

    /**
     * 批次依 code 查 roles（用於 replaceRoles，一次查出所有角色避免 N+1）
     */
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsRole> findAllByCodeIn(Collection<String> codes);
}
