package com.xk.truck.upms.controller.api.dto.system;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * DTO : UpmsSystemListResp
 * Purpose :
 * - 後台 System 列表頁專用 DTO
 * - 僅承載「顯示用」欄位，避免 controller 直接碰 entity
 * <p>
 * Design Notes :
 * - 不包含 Permission / Role 關聯（避免 N+1 與耦合）
 * - 與 UpmsSystemQuery 對齊，方便查詢與顯示
 * ===============================================================
 */
@Data
public class UpmsSystemListResp {

    /**
     * System UUID
     */
    private UUID id;

    /**
     * 系統代碼（唯一，如：UPMS / FMS / TOM）
     */
    private String code;

    /**
     * 系統名稱（顯示用，如：使用者權限管理）
     */
    private String name;

    /**
     * 是否啟用
     */
    private Boolean enabled;

    /**
     * 排序用（後台拖拉或排序）
     */
    private Integer sortOrder;

    /**
     * 備註 / 說明
     */
    private String remark;

    /**
     * 建立時間
     */
    private ZonedDateTime createdTime;

    /**
     * 更新時間
     */
    private ZonedDateTime updatedTime;
}
