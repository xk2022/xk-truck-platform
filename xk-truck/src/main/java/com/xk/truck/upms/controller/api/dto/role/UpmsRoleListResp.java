package com.xk.truck.upms.controller.api.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ===============================================================
 * DTO: UpmsRoleListResp
 * Layer: Controller DTO (API Response)
 * Purpose:
 * - 後台「角色列表」回傳用 DTO（List/Page）
 * Notes:
 * - 列表 DTO 原則：只放列表需要的欄位，避免塞過多關聯導致 N+1
 * - 若未來要顯示 permissionCodes，可改用 DTO Query 一次查回（避免 entity lazy）
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "UPMS 角色列表回傳 DTO")
public class UpmsRoleListResp {

    // ===============================================================
    // Identity
    // ===============================================================
    @Schema(description = "角色 UUID", example = "7f000001-9a53-1aaf-819a-530abad50008")
    private UUID id;

    @Schema(description = "角色代碼（唯一，建議固定大寫）", example = "SYS_ADMIN")
    private String code;

    @Schema(description = "角色名稱", example = "系統管理員")
    private String name;

    @Schema(description = "角色描述", example = "擁有系統全部管理權限")
    private String description;

    // ===============================================================
    // Status / Sorting
    // ===============================================================
    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序（數字越小越前面）", example = "10")
    private Integer sortOrder;

    @Schema(description = "備註", example = "僅限內部管理使用")
    private String remark;

    // ===============================================================
    // Audit (List commonly needed)
    // ===============================================================
    @Schema(description = "建立時間")
    private ZonedDateTime createdAt;

    @Schema(description = "更新時間")
    private ZonedDateTime updatedAt;

    // ===============================================================
    // Optional / Future fields (keep low coupling)
    // ===============================================================
    /**
     * 可選：權限代碼集合（列表通常不建議帶，避免 query N+1）
     * - 若你真的要顯示：建議用 repository DTO query 一次 select 回來
     * - 此欄位先保留擴充點，預設可為 null 或 empty
     */
    @Schema(description = "權限代碼集合（可選；建議用 DTO Query 一次查回）", example = "[\"USER_READ\",\"USER_WRITE\"]")
    private Set<String> permissionCodes = new LinkedHashSet<>();

    /**
     * 可選：綁定系統資訊（如果你 UpmsRole 有 systemUuid / systemCode）
     * - 這裡用扁平欄位避免 DTO 牽扯到 UpmsSystem 物件
     */
    @Schema(description = "所屬系統 UUID（可選）")
    private UUID systemId;

    @Schema(description = "所屬系統代碼（可選）", example = "XK_TRUCK")
    private String systemCode;
}
