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
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    @Comment("權限代碼（唯一）例如：FMS_VEHICLE_READ")
    private String code;

    @NotBlank(message = "權限名稱不能為空")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    @Comment("權限名稱（顯示用）")
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    @Comment("權限描述")
    private String description;

    @Column(nullable = false)
    @Comment("是否啟用（true:啟用, false:停用）")
    private Boolean enabled = true;

    // ===============================================================
    // System / Module boundary（避免硬耦合到 UpmsSystem entity）
    // ===============================================================
    /**
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

    @Size(max = 80)
    @Column(name = "module_code", length = 80)
    @Comment("模組代碼（例如：USER / ROLE / VEHICLE / DRIVER）")
    private String moduleCode;

    // ===============================================================
    // Resource / Action hints（可選，利於前端顯示與 CRUD 拆分）
    // ===============================================================
    @Size(max = 120)
    @Column(name = "resource", length = 120)
    @Comment("資源名稱（例如：USER / VEHICLE / ORDER）")
    private String resource;

    @Size(max = 40)
    @Column(name = "default_action", length = 40)
    @Comment("預設動作（例如：READ/CREATE/UPDATE/DELETE）")
    private String defaultAction;

    // ===============================================================
    // Sort & UI helpers
    // ===============================================================
    @Column(name = "sort_order")
    @Comment("排序（前端功能樹/列表用）")
    private Integer sortOrder;

    @Size(max = 120)
    @Column(name = "group_key", length = 120)
    @Comment("分組 key（例如：FMS_VEHICLE）用於前端樹狀呈現")
    private String groupKey;

    // ===============================================================
    // Actions（CRUD 細分）
    // ===============================================================
    /**
     * ElementCollection = 最低成本的「可擴充 CRUD 子項目」
     * - 優點：不用多建 entity、schema 簡單、很好查詢/更新
     * - 缺點：如果 actions 需要更豐富欄位（name/desc/active/sort），就要抽 entity
     * <p>
     * 表：upms_permission_action_codes
     * 欄位：permission_uuid, action_code
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "upms_permission_action_codes",
            joinColumns = @JoinColumn(
                    name = "permission_uuid",
                    referencedColumnName = "uuid",
                    foreignKey = @ForeignKey(name = "fk_upms_perm_action_permission_uuid")
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_upms_perm_action_permission_uuid_action_code",
                    columnNames = {"permission_uuid", "action_code"}
            )
    )
    @Column(name = "action_code", length = 40, nullable = false)
    @Comment("權限可用動作代碼（CRUD 等）")
    private Set<String> actionCodes = new LinkedHashSet<>();
    // ===============================================================
    // Constructors (安全建構)
    // ===============================================================
    public UpmsPermission(String code, String name, String systemCode) {
        this.code = normalizeCode(code);
        this.name = name != null ? name.trim() : null;
        this.systemCode = normalizeCode(systemCode);
        this.enabled = true;
    }

    // ===============================================================
    // Domain Methods - Normalize
    // ===============================================================
    public static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    public void changeCode(String newCode) {
        this.code = normalizeCode(newCode);
    }

    public void changeName(String newName) {
        this.name = newName != null ? newName.trim() : null;
    }

    public void changeSystemCode(String newSystemCode) {
        this.systemCode = normalizeCode(newSystemCode);
    }

    // ===============================================================
    // Domain Methods - Actions（避免 Set 地雷：全部走方法）
    // ===============================================================
    public void addActionCode(String actionCode) {
        String c = normalizeCode(actionCode);
        if (c == null || c.isBlank()) return;
        if (this.actionCodes == null) this.actionCodes = new LinkedHashSet<>();
        this.actionCodes.add(c);
    }

    public void removeActionCode(String actionCode) {
        if (this.actionCodes == null || this.actionCodes.isEmpty()) return;
        String c = normalizeCode(actionCode);
        this.actionCodes.remove(c);
    }

    /**
     * 覆蓋式更新 actions（常用：CRUD 全替換）
     * - LinkedHashSet：保留順序（前端顯示友善）
     */
    public void replaceActionCodes(Collection<String> newActionCodes) {
        LinkedHashSet<String> next = new LinkedHashSet<>();
        if (newActionCodes != null) {
            for (String v : newActionCodes) {
                String c = normalizeCode(v);
                if (c != null && !c.isBlank()) next.add(c);
            }
        }
        this.actionCodes.clear();
        this.actionCodes.addAll(next);
    }

    // ===============================================================
    // Convenience snapshot（避免外部直接碰集合）
    // ===============================================================
    public Set<String> getActionCodesSnapshot() {
        if (this.actionCodes == null || this.actionCodes.isEmpty()) return Set.of();
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.actionCodes));
    }

    // ===============================================================
    // equals/hashCode (地雷排除)
    // ===============================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsPermission)) return false;
        UpmsPermission other = (UpmsPermission) o;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // ===============================================================
    // toString（避免 lazy 遞迴）
    // ===============================================================
    @Override
    public String toString() {
        return "UpmsPermission{" +
                "uuid=" + uuid +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", systemCode='" + systemCode + '\'' +
                ", moduleCode='" + moduleCode + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
