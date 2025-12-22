package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ===============================================================
 * DTO: UpmsUserListResp
 * Layer: Controller API DTO (UPMS)
 * Purpose:
 * - 後台「使用者列表」使用的回應 DTO
 * - 搭配分頁回傳 Page<UpmsUserListResp>
 * <p>
 * Notes:
 * - roleCodes：以 code 為主（前端可直接顯示或再做 mapping）
 * - joinedAt：對應 BaseEntity.createdTime（你在 service 內用 user.getCreatedTime()）
 * - lastLoginAt：對應 UpmsUser.lastLoginAt
 * - twoStepsEnabled：預留 MFA（目前固定 false）
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "UPMS 使用者列表回應 DTO")
public class UpmsUserListResp {

    // ------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------
    @Schema(description = "使用者 UUID", example = "7f000001-9a53-1aaf-819a-530abad50008")
    private UUID id;

    @Schema(description = "帳號（唯一）", example = "admin")
    private String username;

    // ------------------------------------------------------------
    // Profile (可選)
    // ------------------------------------------------------------
    @Schema(description = "顯示名稱（profile.name；若無 profile 則可能等於 username）", example = "系統管理員")
    private String name;

    @Schema(description = "Email（profile.email）", example = "admin@example.com")
    private String email;

    @Schema(description = "頭像 URL（profile.avatarUrl）", example = "https://cdn.example.com/avatar/u1.png")
    private String avatarUrl;

    // ------------------------------------------------------------
    // Status
    // ------------------------------------------------------------
    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "是否鎖定（false=正常, true=鎖定）", example = "false")
    private Boolean locked;

    // ------------------------------------------------------------
    // Security / Audit
    // ------------------------------------------------------------
    @Schema(description = "最後登入時間", example = "2025-12-11T15:18:45.41871")
    private LocalDateTime lastLoginAt;

    @Schema(description = "加入時間（建立時間）", example = "2025-12-09T09:35:26.910295")
    private ZonedDateTime joinedAt;

    // ------------------------------------------------------------
    // Roles
    // ------------------------------------------------------------
    @Schema(
            description = "角色代碼集合（依指派順序；例如 SYS_ADMIN）",
            example = "[\"SYS_ADMIN\",\"OPS\"]"
    )
    private Set<String> roleCodes = new LinkedHashSet<>();

    // ------------------------------------------------------------
    // MFA (reserved)
    // ------------------------------------------------------------
    @Schema(description = "是否啟用雙因子（預留欄位）", example = "false")
    private Boolean twoStepsEnabled;
}
