package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsUser;

import jakarta.persistence.QueryHint;

import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: UpmsUserRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 UpmsUser 實體的 CRUD 與常用查詢
 * Notes    :
 * - Spring Data JPA 自動實作常規方法
 * - 以 username 作為主要登入識別欄位（唯一）
 * - list/detail 若需要 profile，使用 EntityGraph 避免 N+1
 * - 修改型 query 務必在 Transaction 中執行
 * ===============================================================
 */
@Repository
public interface UpmsUserRepository
        extends JpaRepository<UpmsUser, UUID>,
        JpaSpecificationExecutor<UpmsUser> {


    // ===============================================================
    // Basic lookups
    // ===============================================================

    /**
     * 以帳號查詢使用者
     */
    Optional<UpmsUser> findByUsername(String username);

    /**
     * 是否已存在相同使用者帳號
     */
    boolean existsByUsername(String username);

    // ===============================================================
    // EntityGraph (避免 list 時 profile N+1；userRoles/role 是否要抓看情境)
    // 注意：EntityGraph + Page 在某些 JPA provider 下會有 count query 行為差異，
    // 但通常 Hibernate OK。若你遇到不穩，改用 DTO query（第3段）。
    // ===============================================================

    /**
     * 取得使用者（同時抓 profile）
     * - 等價於 findById，但附帶 profile，避免後續 lazy N+1
     */
    @EntityGraph(attributePaths = {"profile"})
    @QueryHints({
            // 可選：告訴 Hibernate 這是唯讀查詢，減少 dirty checking 負擔
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Optional<UpmsUser> findWithProfileByUuid(UUID uuid);

    /**
     * 分頁查詢（同時抓 profile）
     * - 注意：若你遇到 Page + EntityGraph 供應商行為差異，再改 DTO query 最穩
     */
    @EntityGraph(attributePaths = {"profile"})
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query(
            value = "select u from UpmsUser u",
            countQuery = "select count(u) from UpmsUser u"
    )
    Page<UpmsUser> findAllWithProfile(Pageable pageable);

    // ===============================================================
    // Admin / Ops convenience
    // ===============================================================

    long countByEnabled(Boolean enabled);

    long countByLocked(Boolean locked);

    /**
     * 更新最後登入時間
     * @return 影響筆數（1=成功, 0=查無此人）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UpmsUser u set u.lastLoginAt = :lastLoginAt where u.uuid = :id")
    int updateLastLoginAt(
            @Param("id") UUID id,
            @Param("lastLoginAt") LocalDateTime lastLoginAt
    );

    /**
     * 更新鎖定狀態/鎖定時間/失敗次數（避免拉整個 entity 出來改）
     * @return 影響筆數（1=成功, 0=查無此人）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UpmsUser u
        set u.locked = :locked,
            u.lockedAt = :lockedAt,
            u.loginFailCount = :failCount
        where u.uuid = :id
    """)
    int updateLockState(
            @Param("id") UUID id,
            @Param("locked") Boolean locked,
            @Param("lockedAt") LocalDateTime lockedAt,
            @Param("failCount") Integer failCount
    );
}
