package com.xk.truck.upms.domain.repository;

import com.xk.truck.upms.domain.model.UpmsUserProfile;

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
 * Repository: UpmsUserProfileRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 UpmsUserProfile （使用者個人資料）實體的 CRUD 與常用查詢
 * Notes    :
 * - 與 User 為 OneToOne 關聯
 * - 用於延伸個人資料（姓名、Email、電話...）
 * - Spring Data JPA 自動實作常規方法
 * - Repository 僅負責資料存取，不處理業務邏輯
 * - UserProfile 屬於「非安全資料」，避免在登入流程中被誤抓
 * - 查詢型方法可使用 EntityGraph 避免 N+1（若需同時抓 User）
 * - 修改型 query 務必包在 Transaction 中
 * ===============================================================
 * <p>
 * 常見使用情境
 * 1) 使用者基本資料顯示（姓名 / Email / Avatar）
 * 2) 後台管理使用者列表（避免拉整個 UpmsUser）
 * 3) 使用者個人資料維護（Profile 編輯）
 * <p>
 * 欄位命名假設（請依你的 UpmsUserProfile 調整）
 * - uuid: UUID 主鍵
 * - user: UpmsUser（OneToOne, FK=upms_user.uuid）
 * - name / email / phone / avatarUrl / position
 * - enabled（可選）
 * - remark
 * - createdTime / updatedTime（BaseEntity）
 * ===============================================================
 */
@Repository
public interface UpmsUserProfileRepository
        extends JpaRepository<UpmsUserProfile, UUID>,
        JpaSpecificationExecutor<UpmsUserProfile> {

    // ===============================================================
    // 1️⃣ Basic lookups（最安全、最常用）
    // ===============================================================

    /**
     * 依 userId 查詢 Profile
     * - 最常用：User Detail / 個人資料頁
     */
    Optional<UpmsUserProfile> findByUser_Uuid(UUID userId);

    /**
     * 是否存在 Profile（通常一個 User 只能有一筆）
     */
    boolean existsByUser_Uuid(UUID userId);

    /**
     * 依 email 查詢（若你允許 email 唯一）
     */
    Optional<UpmsUserProfile> findByEmail(String email);

    /**
     * email 唯一檢查（建立/更新 Profile 時）
     */
    boolean existsByEmail(String email);

    // ===============================================================
    // 2️⃣ EntityGraph / Fetch optimization
    // ===============================================================
    // 只有在「Profile 需要同時顯示 User 帳號資訊」時才用

    /**
     * 取得 Profile（同時抓 User）
     * - 適合後台管理頁顯示 username + profile
     */
    @EntityGraph(attributePaths = {
            "user"
    })
    @QueryHints({
            @QueryHint(
                    name = HibernateHints.HINT_READ_ONLY,
                    value = "true"
            )
    })
    @Query("select p from UpmsUserProfile p where p.uuid = :id")
    Optional<UpmsUserProfile> findDetailByUuid(@Param("id") UUID id);

    /**
     * 分頁查詢 Profile（同時抓 User）
     * - ⚠ 若 Page + EntityGraph 在你環境有問題，改用 DTO Projection
     */
//    @EntityGraph(attributePaths = {
//            "user"
//    })
//    @QueryHints({
//            @QueryHint(
//                    name = HibernateHints.HINT_READ_ONLY,
//                    value = "true"
//            )
//    })
//    Page<UpmsUserProfile> findAllWithUser(Pageable pageable);

    // ===============================================================
    // 3️⃣ DTO / Projection Query（列表最穩）
    // ===============================================================

    /**
     * Projection：Profile 列表用（避免拉整個 entity graph）
     */
    interface ProfileRow {
        UUID getId();

        UUID getUserId();

        String getUsername();

        String getName();

        String getEmail();

        String getPhone();

        String getAvatarUrl();

        String getPosition();

        LocalDateTime getCreatedTime();

        LocalDateTime getUpdatedTime();
    }

    /**
     * 分頁查詢 Profile Row（含 keyword 搜尋）
     */
    @Query("""
                select
                    p.uuid as id,
                    u.uuid as userId,
                    u.username as username,
                    p.name as name,
                    p.email as email,
                    p.phone as phone,
                    p.avatarUrl as avatarUrl,
                    p.position as position,
                    p.createdTime as createdTime,
                    p.updatedTime as updatedTime
                from UpmsUserProfile p
                join p.user u
                where (:keyword is null
                       or lower(u.username) like lower(concat('%', :keyword, '%'))
                       or lower(p.name) like lower(concat('%', :keyword, '%'))
                       or lower(p.email) like lower(concat('%', :keyword, '%')))
                order by p.createdTime desc
            """)
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Page<ProfileRow> pageProfileRows(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ===============================================================
    // 4️⃣ Admin / Ops convenience（寫操作 / bulk）
    // ===============================================================

    /**
     * 更新基本資料（最常用）
     * - 避免整個 entity 拉出來改
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsUserProfile p
                set p.name = :name,
                    p.email = :email,
                    p.phone = :phone,
                    p.avatarUrl = :avatarUrl,
                    p.position = :position,
                    p.remark = :remark,
                    p.updatedTime = :time
                where p.uuid = :id
            """)
    int updateBasicInfo(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("avatarUrl") String avatarUrl,
            @Param("position") String position,
            @Param("remark") String remark,
            @Param("time") LocalDateTime time
    );

    /**
     * 更新 Avatar（常見獨立操作）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsUserProfile p
                set p.avatarUrl = :avatarUrl,
                    p.updatedTime = :time
                where p.uuid = :id
            """)
    int updateAvatar(
            @Param("id") UUID id,
            @Param("avatarUrl") String avatarUrl,
            @Param("time") LocalDateTime time
    );

    /**
     * 更新最後異動時間（範例）
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update UpmsUserProfile p
                set p.updatedTime = :time
                where p.uuid = :id
            """)
    int updateUpdatedTime(
            @Param("id") UUID id,
            @Param("time") LocalDateTime time
    );

    /**
     * 依 userId 刪除 Profile
     * - 常用於刪除使用者前的清理
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from UpmsUserProfile p
                where p.user.uuid = :userId
            """)
    int deleteByUserUuid(@Param("userId") UUID userId);
}
