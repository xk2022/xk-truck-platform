package com.xk.truck.tom.domain.model;

/**
 * ===============================================================
 * TOM Enum : TomOrderStatus
 * Layer    : TOM Domain
 * Purpose  : 運輸訂單（TOM）的「內部流程狀態」
 * ===============================================================
 *
 * Design Principles:
 * - 僅描述 TOM 世界觀下的訂單安排流程
 * - 不描述訂單是否取消或結案（由 Core OrderStatus 決定）
 * - 不包含任何運輸執行進度（出車 / 在途 / 完成）
 *
 * Usage Notes:
 * - 僅在 Core OrderStatus = OPEN 時有意義
 * - 當 Core OrderStatus ≠ OPEN 時，TomOrderStatus 僅作為歷史最後狀態保留
 *
 * Lifecycle (TOM-only):
 * - NEW      → ACCEPTED → ASSIGNED
 *
 * @author yuan
 * Created on 2025/12/24.
 */
public enum TomOrderStatus {

    /**
     * 新建訂單
     * - 訂單已建立，但尚未受理
     */
    NEW,

    /**
     * 已受理
     * - 訂單已進入可安排階段
     * - 尚未指派任何資源
     */
    ACCEPTED,

    /**
     * 已完成安排
     * - 已完成車輛 / 司機 / 資源的安排
     * - 不代表運輸已開始或完成
     */
    ASSIGNED
}
