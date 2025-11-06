package com.xk.truck.order.controller.api.dto;

import com.xk.truck.order.domain.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderResp {

    private String uuid;
    private String orderNo;
    private String customerName;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime scheduledAt;
    private OrderStatus status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
