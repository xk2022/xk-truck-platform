package com.xk.truck.tom.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateImportOrderSpec {
    private UUID customerUuid;
    private String pickupAddress;
    private String deliveryAddress;

    private ContainerInfo container;
    private RouteInfo route;

    private ImportOrderDetail importDetail;

    private String note;
}
