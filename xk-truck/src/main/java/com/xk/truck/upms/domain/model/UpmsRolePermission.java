package com.xk.truck.upms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : UpmsRolePermission
 * Layer  : Domain Model (UPMS)
 * Purpose: 角色 ↔ 權限 關聯中介實體（Join Entity）
 * ===============================================================
 * <p>
 * 為什麼不用 @ManyToMany？
 * - @ManyToMany 隱性 join table 不好加欄位、難控刪除、難做審計
 * - 關聯行本身在真實系統中「通常需要欄位」：授權時間、授權人、備註、來源…
 * <p>
 * 排雷 / 低耦合
 * 1) Join Entity 用獨立 uuid 當主鍵（最穩）
 * 2) 用 unique constraint 保證 (role_uuid, permission_uuid) 不重複
 * 3) equals/hashCode 只用 uuid，避免 lazy/集合造成爆炸
 * 4) 不在 equals/hashCode / toString 觸碰 role/permission
 * <p>
 * 關聯屬性建議
 * - role / permission 一律 LAZY
 * - 不要 CascadeType.REMOVE 從 join 反向刪 role/permission
 *
 * @author yuan Created on 2025/12/01.
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "upms_role_permission",
        indexes = {
                @Index(name = "idx_urp_role_uuid", columnList = "role_uuid"),
                @Index(name = "idx_urp_permission_uuid", columnList = "permission_uuid"),
                @Index(name = "idx_urp_enabled", columnList = "enabled")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_urp_role_permission",
                columnNames = {"role_uuid", "permission_uuid"}
        )
)
@Schema(description = "UPMS 角色權限關聯（Role ↔ Permission）")
public class UpmsRolePermission extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "關聯 UUID")
    private UUID uuid;

    // ===============================================================
    // Foreign Keys (Columns)
    // ===============================================================
    /**
     * ✅ 建議同時保留 roleUuid / permissionUuid 欄位（read-only）：
     * - 查詢 projection / mapping DTO 很好用
     * - 不必每次都初始化 role/permission 才能拿到 id
     * - 低耦合：service 可以只用 uuid 操作
     */
    @NotNull
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "role_uuid", length = 36, nullable = false)
    @Comment("角色 UUID（FK）")
    private UUID roleUuid;

    @NotNull
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "permission_uuid", length = 36, nullable = false)
    @Comment("權限 UUID（FK）")
    private UUID permissionUuid;

    // ===============================================================
    // Relations (LAZY)
    // ===============================================================
    /**
     * ⚠ 注意：
     * - insertable/updatable=false，避免重複維護 FK
     * - 需要 role 物件時再 join fetch
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_uuid",
            referencedColumnName = "uuid",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_urp_role_uuid")
    )
    @Comment("角色")
    private UpmsRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "permission_uuid",
            referencedColumnName = "uuid",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_urp_permission_uuid")
    )
    @Comment("權限")
    private UpmsPermission permission;

    // ===============================================================
    // Domain fields (可選，但很實用)
    // ===============================================================
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    @Comment("是否啟用（方便做 soft revoke，不一定要刪 row）")
    private Boolean enabled = true;

    @Comment("授權生效時間（可選）")
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Comment("授權失效時間（可選）")
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Comment("授權來源（可選）例如：SYSTEM_INIT / ADMIN_UI / IMPORT")
    @Column(name = "grant_source", length = 50)
    private String grantSource;

    @Comment("備註（可選）")
    @Column(name = "remark", length = 255)
    private String remark;

    // ===============================================================
    // Constructors (安全建構)
    // ===============================================================
    public UpmsRolePermission(UUID roleUuid, UUID permissionUuid) {
        this.roleUuid = roleUuid;
        this.permissionUuid = permissionUuid;
        this.enabled = true;
    }

    public static UpmsRolePermission of(UUID roleUuid, UUID permissionUuid) {
        return new UpmsRolePermission(roleUuid, permissionUuid);
    }

    // ===============================================================
    // Domain methods
    // ===============================================================
    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }

    /**
     * 判斷是否在有效期間內（若未設 from/to，視為永久有效）
     */
    public boolean isEffectiveAt(LocalDateTime now) {
        LocalDateTime t = (now != null) ? now : LocalDateTime.now();
        if (effectiveFrom != null && t.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && !t.isBefore(effectiveTo)) return false; // to 視為「失效起點」
        return true;
    }

    // ===============================================================
    // equals/hashCode (地雷排除)
    // ===============================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsRolePermission)) return false;
        UpmsRolePermission other = (UpmsRolePermission) o;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // ===============================================================
    // toString（避免 lazy / 遞迴）
    // ===============================================================
    @Override
    public String toString() {
        return "UpmsRolePermission{" +
                "uuid=" + uuid +
                ", roleUuid=" + roleUuid +
                ", permissionUuid=" + permissionUuid +
                ", enabled=" + enabled +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveTo=" + effectiveTo +
                '}';
    }
}
