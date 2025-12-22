package com.xk.truck.tom.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderSpec {
    private OrderType orderType;
    private CustomerSnapshot customer;
    private String pickupAddress;
    private String deliveryAddress;
    private ContainerInfo container;
    private RouteInfo route;
    private String shippingCompany;
    private String vesselVoyage;
    private String note;
    private ImportOrderDetail importDetail;
    private ExportOrderDetail exportDetail; // 先保留，之後 export 再補
}
