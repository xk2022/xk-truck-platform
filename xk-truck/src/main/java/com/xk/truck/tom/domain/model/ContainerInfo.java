package com.xk.truck.tom.domain.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * ===============================================================
 * Value Object : ContainerInfo
 * Purpose      : 櫃資訊（共用於 Import / Export）
 * ===============================================================
 */
@Data
public class ContainerInfo {

    private String shippingCompany;
    private String vesselVoyage;

    private String containerNo;     // MSCU1234567
    private String containerType;   // 20GP / 40HQ

    private Integer packageQty;
    private BigDecimal grossWeight;
    private BigDecimal cbm;

    private ZonedDateTime etd;
    private ZonedDateTime eta;
}
