package com.xk.truck.upms.controller.api.dto.role;

import com.xk.base.web.dto.query.BaseKeywordQuery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
@Data
@Schema(description = "UPMS 角色查詢條件")
public class UpmsRoleQuery extends BaseKeywordQuery {

    /**
     * 是否啟用
     * <p>
     * - true  : 只查啟用系統
     * - false : 只查停用系統
     * - null  : 不限制
     */
    private Boolean enabled;
}
