package com.xk.truck.tom.application.usecase.cmd;

import com.xk.truck.tom.domain.model.OrderType;

import lombok.Data;

@Data
public class CreateOrderReq {
    private OrderType orderType;

    private String customerId;
    private String customerName;

    private String containerNo;
    private String containerType;

    private String pol;
    private String pod;

    // import fields
    private String deliveryOrderLocation;

    // export fields
    private String bookingNo;

    // shared for import/export
    private String shippingCompany;
    private String vesselVoyage;

    private String note;
}
