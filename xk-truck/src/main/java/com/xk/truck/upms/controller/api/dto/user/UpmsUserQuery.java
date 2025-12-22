package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * ===============================================================
 * DTO: UpmsUserQuery
 * Layer: Controller API DTO (UPMS)
 * Purpose:
 * - 後台使用者列表查詢條件（搭配 Pageable）
 * <p>
 * 對應 Service buildUserSpec() 的查詢欄位：
 * - username：like 查詢（%keyword%）
 * - enabled：狀態篩選
 * - locked：鎖定篩選
 * - roleCode：以角色 code 篩選（join userRoles.role.code）
 * <p>
 * Notes:
 * - 這是 Query DTO，不建議放太多業務邏輯與轉換。
 * - 參數清洗（trim/lower）你已在 service spec 中處理。
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "UPMS 使用者列表查詢條件")
public class UpmsUserQuery {

    // ------------------------------------------------------------
    // Keyword / Filters
    // ------------------------------------------------------------
    @Schema(
            description = "帳號關鍵字（like 查詢，忽略大小寫）",
            example = "admin"
    )
    private String username;

    @Schema(
            description = "是否啟用（true=啟用, false=停用, null=不篩選）",
            example = "true"
    )
    private Boolean enabled;

    @Schema(
            description = "是否鎖定（true=鎖定, false=正常, null=不篩選）",
            example = "false"
    )
    private Boolean locked;

    @Schema(
            description = "角色代碼篩選（例如 SYS_ADMIN；null/空字串=不篩選）",
            example = "SYS_ADMIN"
    )
    private String roleCode;

    // ------------------------------------------------------------
    // Future reserved fields（你可以先放著，不用在 spec 裡處理）
    // ------------------------------------------------------------

    @Schema(
            description = "（預留）Email 關鍵字（未啟用）",
            example = "example.com"
    )
    private String emailKeyword;

    @Schema(
            description = "（預留）顯示名稱關鍵字（未啟用）",
            example = "祿元"
    )
    private String nameKeyword;
}
