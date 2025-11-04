package com.xk.truck.upms.domain.model.po;

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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 📌 使用者實體類（UPMS 系統）
 * <p>
 * - 定義系統中使用者的基本資料，如帳號、密碼與啟用狀態。
 * - 每位使用者可具備多個角色（Roles）。
 * - 密碼建議以 BCrypt 雜湊後儲存。
 *
 * @author yuan Created on 2025/10/31.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "uuid")   // ✅ 只用 uuid 判等
@Entity
@Table(name = "upms_user",
        indexes = @Index(name = "idx_user_username", columnList = "username"),
        uniqueConstraints = @UniqueConstraint(name = "uk_user_username", columnNames = "username"))
@Schema(description = "使用者實體")
public class User extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @NotBlank(message = "帳號不能為空")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    @Comment("帳號（唯一）")
    private String username;

    @NotBlank(message = "密碼不能為空")
    @Column(nullable = false)
    @Comment("密碼（BCrypt 雜湊）")
    private String password;

    @Column(nullable = false)
    @Comment("是否啟用")
    private Boolean enabled = true;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    @Comment("帳號鎖定狀態（false=正常, true=鎖定）")
    private Boolean locked = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "upms_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Comment("使用者 - 角色關聯")
    private Set<Role> roles = new HashSet<>();

    /**
     * 建構子：建立新使用者。
     *
     * @param username 使用者名稱
     * @param password 密碼（BCrypt 雜湊後）
     * @param enabled  是否啟用
     */
    public User(String username, String password, Boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }
}
