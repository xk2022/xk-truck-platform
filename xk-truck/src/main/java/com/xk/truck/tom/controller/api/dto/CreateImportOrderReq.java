package com.xk.truck.tom.controller.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Schema(description = "建立進口訂單請求")
public class CreateImportOrderReq {

    // ===== base =====
    @Schema(description = "客戶 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID customerUuid;

    @Schema(description = "取件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pickupAddress;

    @Schema(description = "送達地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryAddress;

    @Schema(description = "預計時段")
    private ZonedDateTime scheduledAt;

    // ----- shipping/container（你 TS 類型錯誤提到的必填群） -----
    @Schema(description = "船公司", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shippingCompany;

    @Schema(description = "船名/航次", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vesselVoyage;

    @Schema(description = "櫃號", requiredMode = Schema.RequiredMode.REQUIRED)
    private String containerNo;

    @Schema(description = "櫃型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String containerType;

    @Schema(description = "件數")
    private Integer packageQty;

    @Schema(description = "毛重")
    private BigDecimal grossWeight;

    @Schema(description = "CBM")
    private BigDecimal cbm;

    @Schema(description = "POL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pol;

    @Schema(description = "POD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pod;

    @Schema(description = "ETD")
    private ZonedDateTime etd;

    @Schema(description = "ETA")
    private ZonedDateTime eta;

    // ===== import detail (你 TS 報錯缺的那些) =====
    @Schema(description = "提貨/送貨單地點（CY/CFS/倉庫...）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryOrderLocation;

    @Schema(description = "提單號 BL No")
    private String blNo;

    @Schema(description = "進口報關號", requiredMode = Schema.RequiredMode.REQUIRED)
    private String importDeclNo;

    @Schema(description = "通關放行時間")
    private LocalDateTime customsReleaseTime;

    @Schema(description = "倉庫")
    private String warehouse;

    @Schema(description = "到港通知/文件備註")
    private String arrivalNotice;

    @Schema(description = "備註")
    private String note;
}
