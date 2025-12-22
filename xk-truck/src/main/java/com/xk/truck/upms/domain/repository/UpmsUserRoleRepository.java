package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsUserRole;

import jakarta.persistence.QueryHint;

import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ===============================================================
 * Service Class : UpmsUserRoleRepository
 * Layer         : Domain DAO (JPA Repository)
 * Purpose       : 提供 UpmsUserRole「使用者 ↔ 角色」關聯（授權關聯）CRUD 與常用查詢/批次操作
 * Notes         :
 * - 建議 UpmsUserRole 採「關聯表獨立 Repository」管理，避免直接操作 UpmsUser.userRoles
 * - bulk delete/update 務必 @Transactional + @Modifying
 * - 查詢 roleUuids 採投影（select ur.role.uuid）避免拉整個 role graph
 * ===============================================================
 * <p>
 * 設計重點：
 * - UpmsUserRole 是關聯實體，不是 Aggregate Root
 * - Repository 僅提供「查詢 / 維護關聯」能力
 * - 不負責授權判斷、不拉整個 User / Role
 * <p>
 * ⚠ 排雷說明：
 * - 不用 Set.contains 判斷是否存在關聯（equals/hc 風險）
 * - exists / delete 一律用 DB 層條件
 * - 修改型 query 一律標註 @Transactional
 * ===============================================================
 */
@Repository
public interface UpmsUserRoleRepository
        extends JpaRepository<UpmsUserRole, UUID> {

    // ===============================================================
    // Basic lookups
    // ===============================================================

    /**
     * 查詢指定使用者的所有角色關聯
     * - 常用於「角色覆蓋式指派」前的清理或檢視
     */
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsUserRole> findByUserUuid(UUID userUuid);

    /**
     * 查詢指定角色被哪些使用者使用
     * - 通常用於 role 刪除前的安全檢查
     */
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    List<UpmsUserRole> findByRoleUuid(UUID roleUuid);

    /**
     * 是否存在 user-role 關聯（避免重複 insert）
     * - 依你 UpmsUserRole 的欄位命名：user.uuid / role.uuid
     * - 建議所有 exists 檢查都用 DB，而不是 Set.contains
     */
    boolean existsByUserUuidAndRoleUuid(UUID userUuid, UUID roleUuid);

    /**
     * 查詢單筆關聯（若你需要拿到 UpmsUserRole 做有效期間設定/異動）
     */
    Optional<UpmsUserRole> findByUserUuidAndRoleUuid(UUID userUuid, UUID roleUuid);

    /**
     * 計算使用者擁有角色數
     */
    long countByUserUuid(UUID userUuid);

    // ===============================================================
    // Read optimization（投影 / 避免 N+1）
    // ===============================================================

    /**
     * 只取某使用者的 role uuid 清單（投影）
     * - 用於 replaceRoles 的差集計算
     * - 不載入 UpmsRole Entity，效能更穩
     */
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("""
                select ur.role.uuid
                from UpmsUserRole ur
                where ur.user.uuid = :userUuid
            """)
    Set<UUID> findRoleUuidsByUserUuid(@Param("userUuid") UUID userUuid);

    // ===============================================================
    // Bulk operations（效能關鍵）
    // ===============================================================

    /**
     * 刪除指定角色的所有使用者關聯
     * - 用於 Role 刪除前的關聯清理
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UpmsUserRole ur where ur.roleUuid = :roleUuid")
    int deleteByRoleUuid(@Param("roleUuid") UUID roleUuid);

    /**
     * 批次刪除使用者 × 多角色關聯
     * - 適合「移除部分角色」的情境
     */
    @Modifying
    @Transactional
    @Query("""
              delete from UpmsUserRole ur
              where ur.user.uuid = :userUuid
                and ur.role.uuid in :roleUuids
            """)
    int deleteByUserUuidAndRoleUuids(
            @Param("userUuid") UUID userUuid,
            @Param("roleUuids") Collection<UUID> roleUuids
    );

    // ===============================================================
    // Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================

    /**
     * 刪除：某使用者的全部角色關聯
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsUserRole ur
                where ur.user.uuid = :userUuid
            """)
    int deleteByUserUuid(@Param("userUuid") UUID userUuid);

    /**
     * 刪除：某使用者的「單一」角色關聯（你缺的第一個）
     *
     * @return 影響筆數（1=有刪到, 0=原本就沒有）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsUserRole ur
                where ur.user.uuid = :userUuid
                  and ur.role.uuid = :roleUuid
            """)
    int deleteByUserUuidAndRoleUuid(
            @Param("userUuid") UUID userUuid,
            @Param("roleUuid") UUID roleUuid
    );

    /**
     * 刪除：某使用者的「多個」角色關聯（你缺的 removed 那個）
     *
     * @return 影響筆數
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsUserRole ur
                where ur.user.uuid = :userUuid
                  and ur.role.uuid in :roleUuids
            """)
    int deleteByUserUuidAndRoleUuidIn(
            @Param("userUuid") UUID userUuid,
            @Param("roleUuids") Collection<UUID> roleUuids
    );
}
