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
 * 📌 角色實體類（UPMS 系統）
 * <p>
 * - 用於描述使用者的權限分組。
 * - 每個角色可對應多個權限（Permissions）。
 * - 與 User 實體形成多對多關聯。
 *
 * @author yuan Created on 2025/10/31.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "upms_role",
        indexes = @Index(name = "idx_role_code", columnList = "code"),
        uniqueConstraints = @UniqueConstraint(name = "uk_role_code", columnNames = "code"))
@Schema(description = "角色實體")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @NotBlank(message = "角色代碼不能為空")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    @Comment("角色代碼（唯一）")
    private String code;

    @NotBlank(message = "角色名稱不能為空")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Comment("角色名稱")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "upms_role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Comment("角色 - 權限關聯")
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 建構子：建立新角色。
     *
     * @param code 角色代碼
     * @param name 角色名稱
     */
    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
