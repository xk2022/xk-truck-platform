package com.xk.truck.tom.application.usecase.cmd;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * UseCase Interface : CreateImportOrderCmd
 * Layer             : Application
 * Purpose           : 建立進口訂單（IMPORT）
 * ===============================================================
 * <p>
 * 👉 Cmd 是「UseCase 專用輸入模型」
 * 不等於 Controller Req
 * 不等於 Entity
 * 是「已經通過 API 驗證後，準備執行業務的命令」
 */
@Data
public class CreateImportOrderCmd {
    private UUID customerUuid;
    private String pickupAddress;
    private String deliveryAddress;
    private ZonedDateTime scheduledAt;

    private String shippingCompany;
    private String vesselVoyage;
    private String containerNo;
    private String containerType;
    private Integer packageQty;
    private BigDecimal grossWeight;
    private BigDecimal cbm;
    private String pol;
    private String pod;
    private ZonedDateTime etd;
    private ZonedDateTime eta;

    // import detail
    private String deliveryOrderLocation;
    private String blNo;
    private String importDeclNo;
    private LocalDateTime customsReleaseTime;
    private String warehouse;
    private String arrivalNotice;

    private String note;
}
