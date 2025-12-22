package com.xk.truck.tom.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Aggregate Root : Order
 * Layer          : Domain Model (TOM)
 * Purpose        : 訂單聚合根（共用欄位 + Import/Export Detail）
 * Notes          :
 * - 不含持久化註解（純 Domain）
 * - 狀態變更與時間更新由 Aggregate 自己維護
 * ===============================================================
 */
@Data
public class Order {

    // ===============================================================
    // Identity
    // ===============================================================
    private UUID uuid;
    private String orderNo;

    // ===============================================================
    // Core
    // ===============================================================
    private OrderType orderType;
    private OrderStatus orderStatus;

    private CustomerSnapshot customer;     // VO：避免只存 customerName
    private ContainerInfo container;       // VO：櫃號/櫃型等
    private RouteInfo route;               // VO：POL/POD

    private String pickupAddress;
    private String deliveryAddress;
    private String note;

    private ZonedDateTime createdTime;
    private ZonedDateTime updatedTime;

    // ===============================================================
    // Type-specific detail (exactly one)
    // ===============================================================
    private ImportOrderDetail importDetail;
    private ExportOrderDetail exportDetail;
}
