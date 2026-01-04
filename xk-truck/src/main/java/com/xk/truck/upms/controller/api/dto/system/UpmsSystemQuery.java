package com.xk.truck.upms.controller.api.dto.system;

import com.xk.base.web.dto.query.BaseKeywordQuery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ===============================================================
 * DTO : UpmsSystemQuery
 * Purpose :
 * - System 後台列表查詢條件
 * - 搭配 JPA Specification 動態組條件
 * <p>
 * Query Strategy :
 * - keyword : like code OR name
 * - enabled : 狀態篩選
 * <p>
 * Design Notes :
 * - 欄位全部 optional
 * - query == null → 全查
 * ===============================================================
 */
@Data
@Schema(description = "UPMS 系統查詢條件")
public class UpmsSystemQuery extends BaseKeywordQuery {

    /**
     * 是否啟用
     * <p>
     * - true  : 只查啟用系統
     * - false : 只查停用系統
     * - null  : 不限制
     */
    private Boolean enabled;
}
