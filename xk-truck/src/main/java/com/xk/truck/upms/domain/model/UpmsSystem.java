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

import java.util.Locale;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : UpmsSystem
 * Layer  : Domain Model (UPMS)
 * Purpose: 系統註冊表（UPMS/ADM/FMS/TOM...）
 * ===============================================================
 * <p>
 * 設計目標
 * 1) 作為「系統維度」的核心資料：系統代碼、名稱、啟用狀態、排序、描述等
 * 2) 支援多系統切換、多系統權限治理（以 code 作為邊界）
 * <p>
 * 排雷 / 低耦合策略
 * - 不要在這裡 @OneToMany 綁 Permission/Role/User：避免大型 join、避免耦合擴散
 * - 授權關聯：用 permission.systemCode / role.systemCode 等欄位或 join entity 在 service/query 組裝
 * - equals/hashCode：只用 uuid，避免集合/關聯造成爆炸
 * - 不用 Lombok @Data：避免 toString/equals 觸發 lazy / 遞迴
 * <p>
 * code 命名建議（唯一且穩定）
 * - UPMS / ADM / FMS / TOM / CMS / ROTARACT
 * - 全大寫、短且固定（當作外鍵/邊界 key）
 *
 * @author yuan
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "upms_system",
        indexes = {
                @Index(name = "idx_upms_system_code", columnList = "code"),
                @Index(name = "idx_upms_system_enabled", columnList = "enabled"),
                @Index(name = "idx_upms_system_sort_order", columnList = "sort_order")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_upms_system_code", columnNames = "code")
)
@Schema(description = "UPMS 系統註冊表（System Registry）")
public class UpmsSystem extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "系統 UUID")
    private UUID uuid;

    // ===============================================================
    // Core fields (穩定識別)
    // ===============================================================
    @NotBlank(message = "系統代碼不能為空")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    @Comment("系統代碼（唯一，建議全大寫）例如：UPMS / ADM / FMS / TOM")
    @Schema(description = "系統代碼（唯一）", example = "FMS")
    private String code;

    @NotBlank(message = "系統名稱不能為空")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    @Comment("系統名稱（顯示用）")
    @Schema(description = "系統名稱", example = "車輛運輸管理系統")
    private String name;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    @Comment("系統描述")
    @Schema(description = "系統描述")
    private String description;

    @Column(name = "enabled", nullable = false)
    @Comment("是否啟用（true:啟用, false:停用）")
    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled = true;

    @Column(name = "sort_order")
    @Comment("排序（前端側邊欄/系統切換排序）")
    @Schema(description = "排序用序號，數字越小越前面", example = "10")
    private Integer sortOrder;

    @Size(max = 255)
    @Column(name = "remark", length = 255)
    @Comment("備註（維運/管理用）")
    @Schema(description = "備註")
    private String remark;

    // ===============================================================
    // Optional: UI / Routing helper（仍維持低耦合）
    // ===============================================================
    @Size(max = 120)
    @Column(name = "base_path", length = 120)
    @Comment("前端 base path（可選）例如：/upms /fms /tom")
    private String basePath;

    @Size(max = 120)
    @Column(name = "icon", length = 120)
    @Comment("圖示代碼（可選）例如：element-plus / car / clipboard")
    private String icon;

    // ===============================================================
    // Constructors (安全建構)
    // ===============================================================
    public UpmsSystem(String code, String name) {
        this.code = normalizeCode(code);
        this.name = name != null ? name.trim() : null;
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

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }

    // ===============================================================
    // equals/hashCode (地雷排除)
    // ===============================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsSystem)) return false;
        UpmsSystem other = (UpmsSystem) o;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // ===============================================================
    // toString（避免遞迴 / lazy）
    // ===============================================================
    @Override
    public String toString() {
        return "UpmsSystem{" +
                "uuid=" + uuid +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
