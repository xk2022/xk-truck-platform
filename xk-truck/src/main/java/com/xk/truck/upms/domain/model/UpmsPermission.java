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
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ===============================================================
 * Entity : UpmsPermission
 * Layer  : Domain Model (UPMS)
 * Purpose: 權限主實體（資源/能力定義）
 * ===============================================================
 * <p>
 * - 用於描述系統可控資源的操作權限，例如 "USER_VIEW"、"USER_EDIT"。
 * - 每個角色可擁有多個權限。
 * - 權限代碼（code）需唯一。
 * <p>
 * 設計原則（低耦合 / 避坑）
 * 1) Permission 不依賴 User/Role（避免授權模型耦合擴散）
 * 2) Role ↔ Permission 用 Join Entity（UpmsRolePermission）維護關聯
 * 3) 避免 Lombok @Data（toString/equals/hashCode 容易踩 lazy / 遞迴）
 * 4) equals/hashCode 只看 uuid（避免包含集合/關聯造成爆炸）
 * <p>
 * 權限 code 建議命名規範
 * - {SYSTEM}_{MODULE}_{RESOURCE}_{ACTION}
 * - 例：UPMS_USER_READ / FMS_VEHICLE_CREATE / TOM_ORDER_UPDATE
 * <p>
 * Actions（CRUD 細分）
 * - 此 Entity 本身不強迫綁 Action entity，避免過度建模造成高耦合
 * - 但提供「支援 actionCodes」的欄位（以 ElementCollection 最低成本）
 * - 你也可以之後抽成 UpmsPermissionAction entity（v2/v3 再做）
 *
 * @author yuan Created on 2025/10/31.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "upms_permission",
        indexes = {
                @Index(name = "idx_upms_permission_code", columnList = "code"),
                @Index(name = "idx_upms_permission_system", columnList = "system_code"),
                @Index(name = "idx_upms_permission_enabled", columnList = "enabled")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_upms_permission_code", columnNames = "code")
)
@Schema(description = "UPMS 權限主實體（資源/能力定義）")
public class UpmsPermission extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "權限 UUID")
    private UUID uuid;

    // ===============================================================
    // Core fields (穩定識別)
    // ===============================================================
    @NotBlank(message = "權限代碼不能為空")
    @Column(nullable = false, updatable = false) // immutable 不可變的
    @Comment("權限代碼（唯一）例如：TOM_ORDER_READ")
    private String code;

    // ===============================================================
    // Domain Classification
    // ===============================================================
    /**
     // ===============================================================
     // System / Module boundary（避免硬耦合到 UpmsSystem entity）
     // ===============================================================
     * 低耦合建議：
     * - 先用 systemCode（字串）做邊界歸屬，不硬綁 @ManyToOne UpmsSystem
     * - 好處：避免權限表一堆 join、避免系統表的改動影響授權核心
     * - 之後真的需要顯示 System 名稱再由查詢投影或 service 組裝即可
     */
    @NotBlank(message = "systemCode 不能為空")
    @Size(max = 80)
    @Column(name = "system_code", nullable = false, length = 80)
    @Comment("系統代碼（例如：UPMS / FMS / TOM）")
    private String systemCode;

    /**
     // ===============================================================
     // Resource / Action hints（可選，利於前端顯示與 CRUD 拆分）
     // ===============================================================
     */
    @Size(max = 120)
    @Column(name = "resource_code", nullable = false, length = 120)
    @Comment("資源代碼（例如：USER / ROLE / VEHICLE / DRIVER / ORDER）")
    private String resourceCode;

    @Size(max = 80)
    @Column(name = "action_code", nullable = false, length = 40)
    @Comment("動作代碼（例如：CREATE / READ / UPDATE / DELETE / EXPORT）")
    private String actionCode;

    // ===============================================================
    // Display / UI
    // ===============================================================

    @NotBlank(message = "權限名稱不能為空")
    @Column(nullable = false, length = 200)
    @Comment("權限名稱（顯示用）例如：訂單-查詢")
    private String name;

    @Column(length = 500)
    @Comment("權限描述")
    private String description;

    @Column(nullable = false)
    @Comment("是否啟用（true:啟用, false:停用）")
    private Boolean enabled = true;

    /**
     // ===============================================================
     // Sort & UI helpers
     // ===============================================================
     */
    @Column(name = "sort_order", nullable = false)
    @Comment("排序（前端功能樹/列表用）")
    private Integer sortOrder = 0;

    @Size(max = 120)
    @Column(name = "group_key", length = 120)
    @Comment("分組 key（例如：TOM_ORDER）用於前端樹狀呈現")
    private String groupKey;

    // ===============================================================
    // L4-ready
    // ===============================================================
    @Column(nullable = false)
    private Long version = 0L;

    @Column
    private Instant deletedAt;

    // ===============================================================
    // Factory
    // ===============================================================
    public static UpmsPermission create(
            String systemCode,
            String resourceCode,
            String actionCode,
            String name
    ) {
        Objects.requireNonNull(systemCode, "systemCode is required");
        Objects.requireNonNull(resourceCode, "resourceCode is required");
        Objects.requireNonNull(actionCode, "actionCode is required");
        Objects.requireNonNull(name, "name is required");

        UpmsPermission p = new UpmsPermission();
        p.systemCode = norm(systemCode);
        p.resourceCode = norm(resourceCode);
        p.actionCode = norm(actionCode);
        p.code = p.systemCode + "_" + p.resourceCode + "_" + p.actionCode;
        p.groupKey = p.systemCode + "_" + p.resourceCode;
        p.name = name.trim();
        p.enabled = true;
        p.sortOrder = 0;
        return p;
    }

    private static String norm(String v) {
        return v == null ? null : v.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeCode(@NotBlank @Size(max = 80) String code) {
        if (!StringUtils.hasText(code)) return null;
        return code.trim().toUpperCase(Locale.ROOT);
    }

    // equals / hashCode 只看 uuid（你這點做得完全正確）
}
