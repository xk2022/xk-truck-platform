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
 * Entity : UpmsRole
 * Layer  : Domain Model (UPMS)
 * Purpose: 角色主實體（授權單元 / 權限集合的容器）
 * ===============================================================
 * <p>
 * - 用於描述使用者的權限分組。
 * - 透過 UserRole 與 User 實體形成多對多關聯。
 * - 透過 RolePermission 與 Permission 關聯，每個角色可對應多個權限。
 * <p>
 * 設計原則（避免高耦合 / 踩雷）
 * 1) UpmsRole 只負責「角色主檔」資料，不直接持有 Permission 集合（避免 @ManyToMany）
 * 2) Role ↔ Permission 請使用關聯實體 UpmsRolePermission（可擴充 action/scope）
 * 3) Role ↔ User 請使用 UpmsUserRole（可加 effectiveFrom/effectiveTo）
 * 4) 避免 Lombok @Data（toString/equals/hashCode 會觸發 lazy 或遞迴）
 * 5) equals/hashCode 僅使用 uuid，避免集合欄位造成 HashSet/Orphan/merge 爆炸
 * <p>
 * 建議的關聯（低耦合）
 * - 不強制在 Role 端維護 userRoles / rolePermissions 集合（避免載入與刪除陷阱）
 * - 若需要雙向關聯，可「選配」加上 List 並且禁止 orphanRemoval（見下方註解）
 *
 * @author yuan Created on 2025/10/31.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "upms_role",
        indexes = {
                @Index(name = "idx_upms_role_code", columnList = "code"),
                @Index(name = "idx_upms_role_enabled", columnList = "enabled"),
                @Index(name = "idx_upms_role_sort_order", columnList = "sort_order")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_upms_role_code", columnNames = "code")
)
@Schema(description = "UPMS 角色主實體（授權單元）")
public class UpmsRole extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "角色 UUID")
    private UUID uuid;

    // ===============================================================
    // Core fields (穩定識別)
    // ===============================================================
    /**
     * 角色 code 強烈建議不可修改（updatable=false）
     * - 權限系統、前端 cache、seed、RBAC 都會依賴 code
     * - 如果要改 code，建議「新建角色 + 遷移關聯」而不是直接 update
     */
    @NotBlank(message = "角色代碼不能為空")
    @Size(max = 80)
    @Column(name = "code", nullable = false, length = 80, updatable = false)
    @Comment("角色代碼（唯一，不可修改；例：SYS_ADMIN / COMPANY_ADMIN / DISPATCH / DRIVER）")
    private String code;

    @NotBlank(message = "角色名稱不能為空")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    @Comment("角色名稱（例：系統管理員、公司管理員、調度員、司機）")
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    @Comment("角色描述（可用於說明權限邊界、適用場景）")
    private String description;

    @Column(name = "enabled", nullable = false)
    @Comment("是否啟用（停用後不應再被指派，且不應生效）")
    private Boolean enabled = true;

    /**
     * 顯示排序
     * - Sidebar / 下拉選單 / 角色列表都可以用
     * - null 表示不特別排序（由 createdTime 等決定）
     */
    @Column(name = "sort_order")
    @Comment("排序（越小越前）")
    private Integer sortOrder;

    @Column(name = "remark", length = 255)
    @Comment("備註（內部維運用）")
    private String remark;

    // ===============================================================
    // Relations (低耦合：不在 Role 端硬綁集合)
    // ===============================================================
    /**
     * 低耦合建議：不要在 Role 端持有 userRoles / rolePermissions 集合
     *
     * 原因：
     * - 很容易造成 lazy 載入、序列化遞迴、N+1、更新時 orphanRemoval 誤刪
     * - 在 RBAC 設計中 Role 是主檔，關聯應由 join entity 管理
     *
     * 如果你真的要在 Role 端也看到關聯（例如要做 Role 詳情頁顯示 permissions），
     * 建議用 List + LAZY + 不要 orphanRemoval（避免覆蓋更新時整批刪除）
     *
     * 例如（選配）：
     *
     * @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
     * private List<UpmsRolePermission> rolePermissions = new ArrayList<>();
     *
     * @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
     * private List<UpmsUserRole> userRoles = new ArrayList<>();
     *
     * ⚠️ 不建議：
     * - Set + equals/hashCode 不穩
     * - orphanRemoval=true 在覆蓋式更新時很容易誤刪資料
     */


    // ===============================================================
    // Constructors (安全建構)
    // ===============================================================

    /**
     * 建立角色（建議由 service 驗證 code 格式後再建立）
     */
    public UpmsRole(String code, String name, Boolean enabled) {
        this.code = normalizeCode(code);
        this.name = normalizeName(name);
        this.enabled = enabled != null ? enabled : true;
    }

    // ===============================================================
    // Domain Methods - Normalize
    // ===============================================================

    /**
     * 統一 role code 格式，避免大小寫混亂造成 ACL 對不上
     * - 建議保存：trim + UPPER + underscore style（由 service 做格式驗證更好）
     */
    public static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 名稱通常 trim 即可
     */
    public static String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    /**
     * ⚠️ code 不可變（updatable=false），所以不提供 changeCode()
     * 如果一定要換 code：建議新建角色並遷移關聯（UserRole/RolePermission）
     */

    public void changeName(String newName) {
        this.name = normalizeName(newName);
    }

    public void changeDescription(String newDesc) {
        this.description = newDesc == null ? null : newDesc.trim();
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    /**
     * 排序欄位變更（可用於 UI 自訂排序）
     */
    public void changeSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ===============================================================
    // Domain Methods - Guard / Validation helpers (不綁策略)
    // ===============================================================

    /**
     * 角色是否可被指派（簡易 guard）
     * - 不包含「租戶/公司/系統」策略，避免 entity 綁定 policy
     */
    public boolean isAssignable() {
        return Boolean.TRUE.equals(this.enabled);
    }

    // ===============================================================
    // equals/hashCode (地雷排除)
    // ===============================================================
    /**
     * equals/hashCode 建議只用 uuid
     * - uuid persist 前可能為 null
     * - hashCode 固定回傳常數，避免 uuid 變更前後造成 HashSet 失效
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpmsRole)) return false;
        UpmsRole other = (UpmsRole) o;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // ===============================================================
    // toString (避免 lazy 遞迴)
    // ===============================================================
    /**
     * 明確 toString，避免 Lombok 自動生成把關聯集合印出來造成遞迴或 lazy 初始化
     */
    @Override
    public String toString() {
        return "UpmsRole{" +
                "uuid=" + uuid +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
