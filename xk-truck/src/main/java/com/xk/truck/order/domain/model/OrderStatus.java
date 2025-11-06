package com.xk.truck.order.domain.model;

public enum OrderStatus {
    CREATED,       // 建立
    ACCEPTED,      // 接單
    DISPATCHED,    // 派送中
    PICKED_UP,     // 已取件
    DELIVERED,     // 已送達
    COMPLETED,     // 已完成
    CANCELED       // 已取消
}
