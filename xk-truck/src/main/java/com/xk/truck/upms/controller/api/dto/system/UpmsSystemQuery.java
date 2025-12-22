package com.xk.truck.upms.controller.api.dto.system;

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
public class UpmsSystemQuery {

    /**
     * 關鍵字搜尋
     * <p>
     * 使用方式：
     * - code LIKE %keyword%
     * - name LIKE %keyword%
     * <p>
     * 範例：
     * - keyword=upms
     * - keyword=權限
     */
    private String keyword;

    /**
     * 是否啟用
     * <p>
     * - true  : 只查啟用系統
     * - false : 只查停用系統
     * - null  : 不限制
     */
    private Boolean enabled;
}
