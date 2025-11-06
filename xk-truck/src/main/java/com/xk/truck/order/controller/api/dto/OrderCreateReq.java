package com.xk.truck.order.controller.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateReq {

    @NotBlank
    @Size(max = 32)
    private String orderNo;

    @NotBlank
    @Size(max = 64)
    private String customerName;

    @NotBlank
    @Size(max = 255)
    private String pickupAddress;

    @NotBlank
    @Size(max = 255)
    private String deliveryAddress;

    private LocalDateTime scheduledAt;
}
