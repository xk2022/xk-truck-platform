package com.xk.truck.tom.application.dto.cmd;

import com.xk.truck.tom.domain.model.TomOrderType;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Application Command
 * - 系統內部語言（不含 HTTP / JSON 語意）
 */
@Data
public class CreateTomOrderCommand {

    TomOrderType orderType;

    UUID customerUuid;
    String customerName;

    String pickupAddress;
    String deliveryAddress;

    ZonedDateTime scheduledAt;
    String customerRefNo;
    String remark;
}
