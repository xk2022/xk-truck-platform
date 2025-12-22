package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ===============================================================
 * DTO: UpmsRoleQuery
 * Layer: Controller DTO (API Query)
 * Purpose:
 * - 後台「角色列表」查詢條件（搭配 Pageable）
 *
 * Design:
 * - 欄位保持「可選」：null 表示不套用該條件
 * - code/name 使用關鍵字查詢（like）
 * - enabled 使用精準篩選（equal）
 * - 預留 systemCode / permissionCode 供後續擴充
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "UPMS 角色查詢條件")
public class UpmsRoleQuery {

    // ===============================================================
    // Keyword filters
    // ===============================================================
    @Schema(description = "角色代碼關鍵字（like；建議可忽略大小寫）", example = "ADMIN")
    private String code;

    @Schema(description = "角色名稱關鍵字（like）", example = "管理")
    private String name;

    // ===============================================================
    // Exact filters
    // ===============================================================
    @Schema(description = "是否啟用（null=不篩選）", example = "true")
    private Boolean enabled;

    // ===============================================================
    // Optional / Future: System scope
    // ===============================================================
    /**
     * 若你未來做到多系統：
     * - role 可以屬於某個 system（systemCode/systemUuid）
     * - 查詢時常常會依 system 做過濾
     */
    @Schema(description = "系統代碼（可選；多系統時常用）", example = "XK_TRUCK")
    private String systemCode;

    // ===============================================================
    // Optional / Future: Permission filtering (costly, use carefully)
    // ===============================================================
    /**
     * 以單一 permissionCode 篩選角色（可能需要 join role_permissions）
     * - 注意：join 會讓 count query 變慢、需要 distinct
     * - 建議資料量大時改用子查詢或先查 roleIds 再查 roles
     */
    @Schema(description = "包含指定權限代碼的角色（可選；可能需要 join）", example = "USER_WRITE")
    private String permissionCode;

    /**
     * 若你要支援「多個 permissionCodes」任一符合：
     * - 會更重，建議做成進階查詢選項
     */
    @Schema(description = "包含任一權限代碼的角色（可選；進階）", example = "[\"USER_READ\",\"USER_WRITE\"]")
    @Builder.Default
    private Set<String> permissionCodes = new LinkedHashSet<>();
}
