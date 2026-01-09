package com.xk.truck.ordercore.application.dto;

import com.xk.truck.ordercore.domain.model.OrderCoreStatus;

import lombok.Data;

import java.util.UUID;

/**
 * ===============================================================
 * Use Case Result: OrderCoreResult
 * Layer          : Application (Use Case Output)
 * Purpose        : 回傳 order-core 建立完成後的核心結果
 * ===============================================================
 * <p>
 * Design Notes:
 * - 作為 order-core Use Case 的輸出模型
 * - 僅包含跨 Domain 可見的「命運層」資訊
 * - 不暴露 Entity / Aggregate / Persistence 細節
 * <p>
 * Typical Usage:
 * - TOM / COM / SOM 在建立流程後取得 orderUuid
 * - 作為後續 Domain Aggregate 的共享主鍵
 */
@Data
public class OrderCoreResult {

    /**
     * 系統內部唯一訂單識別
     */
    UUID orderUuid;

    /**
     * 全系統訂單編號（人類可讀）
     */
    String orderNo;

    /**
     * 訂單命運層狀態（OPEN / CANCELLED / CLOSED）
     */
    OrderCoreStatus orderCoreStatus;
}
