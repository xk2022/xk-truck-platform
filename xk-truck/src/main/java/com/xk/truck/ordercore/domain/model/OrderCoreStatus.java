package com.xk.truck.ordercore.domain.model;

/**
 * ===============================================================
 * Core Enum : OrderCoreStatus
 * Layer     : Order Core (Cross-Domain)
 * Purpose   : 訂單的「高階生命週期狀態（命運層）」
 * ===============================================================
 * <p>
 * Design Principles:
 * - 描述訂單是否仍屬於「有效交易」
 * - 不描述任何業務流程或執行細節
 * - 作為跨 Domain（TOM / COM / SOM / FIN / FMS）的共同語言
 * <p>
 * Usage Notes:
 * - OPEN       : 訂單仍處於進行中（尚未取消或結案）
 * - CANCELLED : 訂單已被終止（主動或被動）
 * - CLOSED    : 訂單已完成並正式結案
 * <p>
 * Mapping Examples:
 * - TOM : NEW / ACCEPTED / ASSIGNED → OPEN
 * - COM : PENDING_PAYMENT / PAID / FULFILLING → OPEN
 * - SOM : DRAFT / QUOTED / APPROVED → OPEN
 *
 * @author yuan
 * Created on 2025/12/24.
 */
public enum OrderCoreStatus {

    /**
     * 訂單仍在進行中
     * - 可持續執行 Domain 內部流程
     * - 允許狀態轉換與資料異動
     */
    OPEN,

    /**
     * 訂單已被取消
     * - 不再允許任何業務或流程操作
     * - 作為「終態」存在
     */
    CANCELLED,

    /**
     * 訂單已完成並結案
     * - 所有業務流程結束
     * - 作為「終態」存在
     */
    CLOSED
}
