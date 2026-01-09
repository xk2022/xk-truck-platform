package com.xk.truck.tom.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Aggregate Root: TomOrder
 * Layer        : Domain (Aggregate Root)
 * Purpose      : 描述「運輸訂單流程」的核心模型與規則
 * ===============================================================
 * Notes:
 * - 與 order-core 共享主鍵：orderUuid
 * - orderNo 為對外識別（由 order-core 同步）
 * - tomStatus 為 TOM 流程狀態（NEW/ACCEPTED/ASSIGNED...）
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TomOrder {

    // ===============================================================
    // Identity (共享主鍵)
    // ===============================================================
    private final UUID orderUuid;
    private final String orderNo;

    // ===============================================================
    // Type & Status
    // ===============================================================
    private final TomOrderType orderType;
    private TomOrderStatus tomStatus;

    // ===============================================================
    // Customer Snapshot
    // ===============================================================
    private final UUID customerUuid;
    private final String customerName;

    // ===============================================================
    // Route
    // ===============================================================
    private final String pickupAddress;
    private final String deliveryAddress;

    // ===============================================================
    // Schedule / Reference / Note
    // ===============================================================
    private final ZonedDateTime scheduledAt;
    private final String customerRefNo;
    private final String remark;

    // ===============================================================
    // Factory: Create (新建)
    // ===============================================================
    public static TomOrder create(
            UUID orderUuid,
            String orderNo,
            TomOrderType orderType,
            UUID customerUuid,
            String customerName,
            String pickupAddress,
            String deliveryAddress,
            ZonedDateTime scheduledAt,
            String customerRefNo,
            String remark
    ) {
        // --- 不變式（MVP 版：必要欄位檢核）---
        require(orderUuid, "orderUuid is required");
        requireText(orderNo, "orderNo is required");
        require(orderType, "orderType is required");
        require(customerUuid, "customerUuid is required");
        requireText(customerName, "customerName is required");
        requireText(pickupAddress, "pickupAddress is required");
        requireText(deliveryAddress, "deliveryAddress is required");

        TomOrder agg = new TomOrder(
                orderUuid,
                orderNo,
                orderType,
                TomOrderStatus.NEW, // 新建一律 NEW（你的註解就是這樣）
                customerUuid,
                customerName,
                pickupAddress,
                deliveryAddress,
                scheduledAt,
                customerRefNo,
                remark
        );

        return agg;
    }

    // ===============================================================
    // Factory: Rehydrate (DB 還原)
    // ===============================================================
    public static TomOrder rehydrate(
            UUID orderUuid,
            String orderNo,
            TomOrderType orderType,
            TomOrderStatus tomStatus,
            UUID customerUuid,
            String customerName,
            String pickupAddress,
            String deliveryAddress,
            ZonedDateTime scheduledAt,
            String customerRefNo,
            String remark
    ) {
        // DB 還原仍要保護基本一致性（避免 null 破壞 aggregate）
        require(orderUuid, "orderUuid is required");
        requireText(orderNo, "orderNo is required");
        require(orderType, "orderType is required");
        require(tomStatus, "tomStatus is required");

        return new TomOrder(
                orderUuid,
                orderNo,
                orderType,
                tomStatus,
                customerUuid,
                customerName,
                pickupAddress,
                deliveryAddress,
                scheduledAt,
                customerRefNo,
                remark
        );
    }

    // ===============================================================
    // Domain Behaviors (業務動作)
    // - 方案A：取消歸 Order-Core（OrderStatus.CANCELLED）
    // - TOM 僅負責「流程面規則」：何時允許取消、何時允許 accept/assign
    // ===============================================================

    public void accept() {
        // NEW -> ACCEPTED
        if (this.tomStatus != TomOrderStatus.NEW) {
            throw new IllegalStateException("Only NEW order can be accepted.");
        }
        this.tomStatus = TomOrderStatus.ACCEPTED;
    }

    public void markAssigned() {
        // ACCEPTED -> ASSIGNED
        if (this.tomStatus != TomOrderStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED order can be assigned.");
        }
        this.tomStatus = TomOrderStatus.ASSIGNED;
    }

    /**
     * 取消（方案A）
     * - TOM 不寫入 CANCELLED（因為取消狀態在 order-core）
     * - 這裡只負責檢查：目前流程狀態是否允許取消
     */
    public void ensureCanCancel() {
        // MVP：ASSIGNED 後不可取消（你可依規則調整）
        if (this.tomStatus == TomOrderStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED order cannot be cancelled.");
        }
    }

    /**
     * （可選）避免 core 已取消仍繼續操作 TOM 流程
     * - UseCase 在操作前，把 coreCancelled 帶進來
     */
    public void ensureActive(boolean coreCancelled) {
        if (coreCancelled) {
            throw new IllegalStateException("Order is cancelled (order-core).");
        }
    }



    // ===============================================================
    // Guards
    // ===============================================================
    private static void require(Object v, String msg) {
        if (v == null) throw new IllegalArgumentException(msg);
    }

    private static void requireText(String v, String msg) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(msg);
    }
}
