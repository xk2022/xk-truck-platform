package com.xk.truck.upms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ===============================================================
 * Entity : UpmsUser
 * Layer  : Domain Model (UPMS)
 * Purpose: 使用者主實體（帳號 / 安全 / 狀態）
 * ===============================================================
 * <p>
 * - 定義系統中使用者的基本資料，如帳號、密碼與啟用狀態。
 * - 每位使用者可具備多個角色（Roles）。
 * - 密碼建議以 BCrypt 雜湊後儲存。
 * <p>
 * 設計原則（避免高耦合 / 踩雷）
 * 1) UpmsUser 是「核心帳號模型」，不要依賴 Permission/Resource，避免耦合擴散
 * 2) 對 Role 採用關聯實體 UpmsUserRole（而不是直接塞 Role List）
 * 3) 透過 domain method 管理安全狀態與角色指派，避免 Service 到處 set 欄位
 * 4) 避免 Lombok @Data（toString/equals/hashCode 會觸發 lazy 或遞迴）
 * <p>
 * 關聯：
 * - 1:1 UpmsUserProfile：延伸個人資料（非安全）
 * - 1:N UpmsUserRole：使用者角色關聯（授權）
 *
 * @author yuan Created on 2025/10/31, Updated on 2025/12/12.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "upms_user",
        indexes = {
                @Index(name = "idx_upms_user_username", columnList = "username"),
                @Index(name = "idx_upms_user_enabled", columnList = "enabled"),
                @Index(name = "idx_upms_user_locked", columnList = "locked")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_upms_user_username", columnNames = "username")
)
@Schema(description = "UPMS 使用者主實體（帳號與安全）")
public class UpmsUser extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "使用者 UUID")
    private UUID uuid;

    // ===============================================================
    // Core fields (穩定識別)
    // ===============================================================
    @NotBlank(message = "帳號不能為空")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    @Comment("帳號（唯一）")
    private String username;

    @NotBlank(message = "密碼不能為空")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    @Comment("密碼（BCrypt 雜湊）")
    private String password;

    @Column(nullable = false)
    @Comment("是否啟用")
    private Boolean enabled = true;

    @Column(nullable = false)
    @Comment("帳號鎖定狀態（false=正常, true=鎖定）")
    private Boolean locked = false;

    @Comment("連續登入失敗次數")
    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount = 0;

    @Comment("最後登入時間")
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Comment("最後鎖定時間（可選，用於解鎖策略 / 稽核）")
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Comment("最後密碼更新時間（可選，用於強制換密碼策略）")
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ===============================================================
    // Profile (非安全資料)
    // ===============================================================
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UpmsUserProfile profile;
    // ===============================================================
    // Roles (授權關聯)
    // ===============================================================
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UpmsUserRole> userRoles = new LinkedHashSet<>();

    // ===============================================================
    // Constructors (安全建構)
    // ===============================================================

    /**
     * 建構子：建立新使用者。
     * 建立新使用者（建議由 Service 負責 encode password 後再帶入）
     *
     * @param username 使用者名稱
     * @param password 密碼（BCrypt 雜湊後）
     * @param enabled  是否啟用
     */
    public UpmsUser(String username, String password, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled != null ? enabled : true;
    }

    // ===============================================================
    // Domain Methods - Username normalize
    // ===============================================================

    /**
     * ✅ 統一 username 格式，避免 Admin/admin 重複、空白問題
     * - 建議統一小寫保存（如需保留顯示名稱，放到 profile.name）
     */
    public static String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    public void changeUsername(String newUsername) {
        this.username = normalizeUsername(newUsername);
    }

    // ===============================================================
    // Domain Methods - Security
    // ===============================================================

    /**
     * 記錄成功登入
     * - 清除 loginFailCount
     * - 更新 lastLoginAt
     */
    public void markLoginSuccess(LocalDateTime now) {
        this.loginFailCount = 0;
        this.lastLoginAt = now != null ? now : LocalDateTime.now();
    }

    /**
     * 記錄登入失敗
     * - 累加 fail count
     * - 若達到閾值可由 service 決定是否 lock（避免 entity 綁定 policy）
     */
    public int markLoginFailed() {
        this.loginFailCount = (this.loginFailCount == null ? 0 : this.loginFailCount) + 1;
        return this.loginFailCount;
    }

    /**
     * 鎖定帳號（由安全策略呼叫）
     */
    public void lock(LocalDateTime now) {
        this.locked = true;
        this.lockedAt = now != null ? now : LocalDateTime.now();
    }

    /**
     * 解鎖帳號
     * - 同時清除 fail count
     */
    public void unlock() {
        this.locked = false;
        this.loginFailCount = 0;
        this.lockedAt = null;
    }

    /**
     * 更新密碼（需由 Service 傳入已 encode 的密碼）
     */
    public void changePassword(String encodedPassword, LocalDateTime now) {
        this.password = encodedPassword;
        this.passwordChangedAt = now != null ? now : LocalDateTime.now();
    }

    // ===============================================================
    // Domain Methods - Profile
    // ===============================================================

    /**
     * 維護雙向關聯
     */
    public void setProfile(UpmsUserProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setUser(this);
        }
    }

    // ===============================================================
    // Domain Methods - Roles (低耦合操作)
    // ===============================================================

    /**
     * 增加角色關聯（由 service 先查好 role，再建立 UpmsUserRole 帶進來）
     * - 避免 UpmsUser 直接依賴 role repository
     */
    public void addUserRole(UpmsUserRole userRole) {
        if (userRole == null) return;
        this.userRoles.add(userRole);
        userRole.setUser(this);
    }

    public void removeUserRole(UpmsUserRole userRole) {
        if (userRole == null) return;
        this.userRoles.remove(userRole);
        userRole.setUser(null);
    }

    /**
     * ✅ 覆蓋式指派角色（最常用）
     * - 由外部組出新的 UpmsUserRole 集合，丟進來覆蓋
     * - orphanRemoval=true 會刪掉舊關聯（正常）
     */
    public void replaceUserRoles(Collection<UpmsUserRole> newRoles) {
        // 清掉舊的雙向關聯
        for (UpmsUserRole ur : new ArrayList<>(this.userRoles)) {
            removeUserRole(ur);
        }
        if (newRoles == null) return;
        for (UpmsUserRole ur : newRoles) {
            addUserRole(ur);
        }
    }

    /**
     * 取得 role codes（避免 controller/service 直接碰 Lazy role）
     * - 若 UpmsUserRole.role 是 LAZY，這裡可能觸發載入
     * - 若你想完全避免 entity 觸發載入，改由 query/mapper 層處理
     */
    public Set<String> getRoleCodesSnapshot() {
        if (this.userRoles == null || this.userRoles.isEmpty()) return Set.of();
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        for (UpmsUserRole ur : this.userRoles) {
            if (ur != null && ur.getRole() != null && ur.getRole().getCode() != null) {
                codes.add(ur.getRole().getCode());
            }
        }
        return codes;
    }

    // ===============================================================
    // equals/hashCode (地雷排除)
    // ===============================================================
    /**
     * Entity equals/hashCode 建議只用 uuid（避免包含關聯集合造成爆炸）
     * 注意：uuid 在 persist 前可能為 null，因此 equals 需處理 null
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsUser)) return false;
        UpmsUser other = (UpmsUser) o;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
