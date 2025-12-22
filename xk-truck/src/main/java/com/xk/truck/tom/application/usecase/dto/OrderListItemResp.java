package com.xk.truck.tom.application.usecase.dto;

import com.xk.truck.tom.domain.model.OrderStatus;
import com.xk.truck.tom.domain.model.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "TOM 訂單列表項目（import/export 一起回）")
public class OrderListItemResp {

    // ===== base =====
    @Schema(description = "訂單 UUID")
    private String id;

    @Schema(description = "訂單編號")
    private String orderNo;

    @Schema(description = "訂單類型")
    private OrderType orderType;

    @Schema(description = "訂單狀態")
    private OrderStatus orderStatus;

    @Schema(description = "客戶 UUID")
    private UUID customerUuid;

    @Schema(description = "取件地址")
    private String pickupAddress;

    @Schema(description = "送達地址")
    private String deliveryAddress;

    @Schema(description = "預計時段")
    private ZonedDateTime scheduledAt;

    // shipping/container（你先放主表）
    @Schema(description = "船公司")
    private String shippingCompany;

    @Schema(description = "船名/航次")
    private String vesselVoyage;

    @Schema(description = "櫃號")
    private String containerNo;

    @Schema(description = "櫃型")
    private String containerType;

    @Schema(description = "建立時間")
    private ZonedDateTime createdTime;

    // ===== import detail =====
    @Schema(description = "提貨/送貨單地點（import）")
    private String importDeliveryOrderLocation;

    @Schema(description = "進口報關號（import）")
    private String importDeclNo;

    @Schema(description = "提單號 BL No（import）")
    private String blNo;

    // ===== export detail（先放最常用的，後面你可以再加）=====
    @Schema(description = "訂艙號 Booking No（export）")
    private String bookingNo;

    @Schema(description = "S/O No（export）")
    private String soNo;
}
