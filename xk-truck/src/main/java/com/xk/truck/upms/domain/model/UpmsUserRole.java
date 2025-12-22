package com.xk.truck.upms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : UpmsUserRole
 * Layer  : Domain Model (UPMS)
 * Purpose: 使用者 ↔ 角色 關聯中介實體
 * ===============================================================
 * <p>
 * - 方便未來加入有效期限等欄位
 * <p>
 * 設計重點：
 * 1) 這不是 Join Table，而是「授權關聯實體」
 * 2) equals/hashCode 不用 id、不用 entity，用 userId + roleId（自然鍵）
 * 3) 明確禁止 Cascade 到 Role（避免誤刪角色）
 * 4) userId / roleId 明確存在，避免 proxy 問題
 *
 * @author yuan Created on 2025/12/01.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "upms_user_role",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_upms_user_role",
                        columnNames = {"user_uuid", "role_uuid"}
                )
        },
        indexes = {
                @Index(name = "idx_upms_user_role_user", columnList = "user_uuid"),
                @Index(name = "idx_upms_user_role_role", columnList = "role_uuid")
        }
)
@Schema(description = "使用者角色關聯（UPMS）")
public class UpmsUserRole extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, nullable = false, updatable = false)
    private UUID uuid;

    // ===============================================================
    // Foreign Keys（核心！）
    // ===============================================================
    @Column(name = "user_uuid", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Comment("使用者 UUID")
    private UUID userUuid;

    @Column(name = "role_uuid", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Comment("角色 UUID")
    private UUID roleUuid;

//    @Column(name = "effective_from")
//    @Comment("關聯生效時間（包含）")
//    private LocalDateTime effectiveFrom;
//
//    @Column(name = "effective_to")
//    @Comment("關聯失效時間（不包含）")
//    private LocalDateTime effectiveTo;

    // ===============================================================
    // Relations（LAZY + 不 cascade）
    // ===============================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_uuid",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private UpmsUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_uuid",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private UpmsRole role;

    // ===============================================================
    // Constructor（安全建構）
    // ===============================================================
    public UpmsUserRole(UpmsUser user, UpmsRole role) {
        this.user = user;
        this.role = role;
        this.userUuid = user.getUuid();
        this.roleUuid = role.getUuid();
    }

    // ===============================================================
    // equals / hashCode（重點！）
    // ===============================================================

    /**
     * 自然鍵比對：
     * - userId + roleId
     * - 不用 uuid（persist 前為 null）
     * - 不用 user / role entity（避免 proxy / lazy 問題）
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsUserRole)) return false;
        UpmsUserRole that = (UpmsUserRole) o;
        return Objects.equals(userUuid, that.userUuid)
                && Objects.equals(roleUuid, that.roleUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUuid, roleUuid);
    }
}
