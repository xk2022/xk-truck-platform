package com.xk.truck.tom.controller.api.dto.resp;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : TomOrderResp
 * Layer            : Interface Adapters (Request) API 輸出格式
 * Purpose          : 提供 TOM 訂單管理 API（Create / List / Detail / Assign / Status）
 * ===============================================================
 */
@Data
public class TomOrderResp {
    UUID id;
    String orderNo;

    String orderType;
    String tomStatus;

    UUID customerUuid;
    String customerName;

    String pickupAddress;
    String deliveryAddress;

    ZonedDateTime scheduledAt;
    String customerRefNo;
    String note;

    List<String> availableActions;
}
