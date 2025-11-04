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

import java.util.UUID;

/**
 * 📌 權限實體類（UPMS 系統）
 * <p>
 * - 用於描述系統可控資源的操作權限，例如 "USER_VIEW"、"USER_EDIT"。
 * - 每個角色可擁有多個權限。
 * - 權限代碼（code）需唯一。
 *
 * @author yuan Created on 2025/10/31.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "upms_permission",
        indexes = @Index(name = "idx_perm_code", columnList = "code"),
        uniqueConstraints = @UniqueConstraint(name = "uk_perm_code", columnNames = "code"))
@Schema(description = "權限實體")
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @NotBlank(message = "權限代碼不能為空")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    @Comment("權限代碼（唯一）")
    private String code;

    @NotBlank(message = "權限名稱不能為空")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Comment("權限名稱")
    private String name;

    /**
     * 建構子：建立新權限。
     *
     * @param code 權限代碼
     * @param name 權限名稱
     */
    public Permission(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
