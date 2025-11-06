package com.xk.truck.order.controller.api.dto;

import com.xk.truck.order.domain.model.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusReq {

    @NotNull
    private OrderStatus to;
}
