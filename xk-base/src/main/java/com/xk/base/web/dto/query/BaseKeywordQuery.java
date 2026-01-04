package com.xk.base.web.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ===============================================================
 * Base DTO: KeywordQuery
 * Purpose:
 * - 提供「關鍵字搜尋」的共通欄位
 * - 適用於後台列表查詢（code / name like）
 *
 * Design Notes:
 * - null 表示不套用關鍵字條件
 * - 不包含任何業務欄位
 * ===============================================================
 */
@Data
public abstract class BaseKeywordQuery {

    @Schema(
            description = "關鍵字搜尋（通常套用於 code / name like）",
            example = "upms"
    )
    private String keyword;

    /**
     * 是否啟用
     * <p>
     * - true  : 只查啟用
     * - false : 只查停用
     * - null  : 不限制
     */
//    @Schema(description = "是否啟用")
//    private Boolean enabled;
}
