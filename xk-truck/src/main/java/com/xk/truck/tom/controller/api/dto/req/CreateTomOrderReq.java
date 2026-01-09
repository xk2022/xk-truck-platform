package com.xk.truck.tom.controller.api.dto.req;

import com.xk.truck.tom.domain.model.TomOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : CreateTomOrderReq
 * Layer            : Interface Adapters (Request) API 輸入格式
 * Purpose          : 提供 TOM 訂單管理 API（Create / List / Detail / Assign / Status）
 * ===============================================================
 */
@Data
public class CreateTomOrderReq {

    @NotNull
    private TomOrderType orderType;

    @NotNull
    private UUID customerUuid;

    // snapshot
    @NotBlank
    @Size(max = 200)
    private String customerName;

    @NotBlank
    @Size(max = 255)
    private String pickupAddress;

    @NotBlank
    @Size(max = 255)
    private String deliveryAddress;

    // optional
    private ZonedDateTime scheduledAt;

    @Size(max = 64)
    private String customerRefNo;

    @Size(max = 500)
    private String remark;
}
