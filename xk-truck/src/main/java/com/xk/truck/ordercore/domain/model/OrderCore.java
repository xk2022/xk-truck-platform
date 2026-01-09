package com.xk.truck.ordercore.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Aggregate Root: OrderCore
 * Layer        : Domain (Order Core)
 * Purpose      : 描述「訂單的命運狀態」
 * <p>
 * Responsibilities:
 * - 訂單是否存在
 * - 訂單是否仍有效（OPEN）
 * - 訂單是否終局（CANCELLED / CLOSED）
 * <p>
 * Non-Responsibilities:
 * - 不描述任何業務流程
 * - 不知道 TOM / COM / SOM 的狀態
 * ===============================================================
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCore {

    // ===============================================================
    // Identity
    // ===============================================================
    private final UUID orderUuid;
    private final String orderNo;

    // ===============================================================
    // Fate Status
    // ===============================================================
    private OrderCoreStatus orderCoreStatus;

    // ===============================================================
    // Audit
    // ===============================================================
    private final ZonedDateTime createdAt;
    private ZonedDateTime cancelledAt;
    private ZonedDateTime closedAt;

    // ===============================================================
    // Factory: Create
    // ===============================================================
    public static OrderCore create(String orderNo) {
        return new OrderCore(
                null,                 // 尚未持久化，UUID 由 infra 產生
                orderNo,
                OrderCoreStatus.OPEN,
                ZonedDateTime.now(),
                null,
                null
        );
    }

    // ===============================================================
    // Factory: Rehydrate（由 Repository 還原）
    // ===============================================================
    public static OrderCore rehydrate(
            UUID orderUuid,
            String orderNo,
            OrderCoreStatus orderCoreStatus,
            ZonedDateTime createdAt,
            ZonedDateTime cancelledAt,
            ZonedDateTime closedAt
    ) {
        return new OrderCore(
                orderUuid,
                orderNo,
                orderCoreStatus,
                createdAt,
                cancelledAt,
                closedAt
        );
    }

    // ===============================================================
    // Domain Behaviors
    // ===============================================================
    public void cancel() {
        if (this.orderCoreStatus == OrderCoreStatus.CLOSED) {
            throw new IllegalStateException("Order already closed");
        }
        if (this.orderCoreStatus == OrderCoreStatus.CANCELLED) return;

        this.orderCoreStatus = OrderCoreStatus.CANCELLED;
        this.cancelledAt = ZonedDateTime.now();
    }

    public void close() {
        if (this.orderCoreStatus == OrderCoreStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        if (this.orderCoreStatus == OrderCoreStatus.CLOSED) return;

        this.orderCoreStatus = OrderCoreStatus.CLOSED;
        this.closedAt = ZonedDateTime.now();
    }

    // ===============================================================
    // Guards
    // ===============================================================
    public boolean isActive() {
        return this.orderCoreStatus == OrderCoreStatus.OPEN;
    }


    // ===============================================================
    // Factory / State helpers
    // ===============================================================

//    public static OrderCoreEntity create(String orderNo) {
//        return OrderCoreEntity.builder()
//                .orderNo(orderNo)
//                .orderStatus(OrderStatus.OPEN)
//                .build(); // createdTime 交給 BaseEntity auditing
//    }
//
//    public void cancel() {
//        if (this.orderStatus == OrderStatus.CLOSED) {
//            throw new BusinessException("ORDER_ALREADY_CLOSED", "訂單已結案，無法取消");
//        }
//        if (this.orderStatus == OrderStatus.CANCELLED) return; // idempotent
//
//        this.orderStatus = OrderStatus.CANCELLED;
//        this.cancelledAt = ZonedDateTime.now();
//    }
//
//    public void close() {
//        if (this.orderStatus == OrderStatus.CANCELLED) {
//            throw new BusinessException("ORDER_ALREADY_CANCELLED", "訂單已取消，無法結案");
//        }
//        if (this.orderStatus == OrderStatus.CLOSED) return; // idempotent
//
//        this.orderStatus = OrderStatus.CLOSED;
//        this.closedAt = ZonedDateTime.now();
//    }
}
