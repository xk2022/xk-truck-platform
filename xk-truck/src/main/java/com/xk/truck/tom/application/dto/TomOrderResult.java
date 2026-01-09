package com.xk.truck.tom.application.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TomOrderResult {

    UUID orderUuid;
    String orderNo;
    String orderType;
    String tomStatus;

    UUID customerUuid;
    String customerName;

    String pickupAddress;
    String deliveryAddress;

    ZonedDateTime scheduledAt;
    String customerRefNo;
    String remark;

    List<String> availableActions;
}
