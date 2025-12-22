package com.xk.truck.tom.infra.persistence.jpa.projection;

import com.xk.truck.tom.domain.model.OrderStatus;
import com.xk.truck.tom.domain.model.OrderType;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface OrderListItemProjection {

    // ===== base =====
    UUID getUuid();
    String getOrderNo();
    OrderType getOrderType();
    OrderStatus getOrderStatus();

    UUID getCustomerUuid();

    String getPickupAddress();
    String getDeliveryAddress();

    String getShippingCompany();
    String getVesselVoyage();

    String getContainerNo();
    String getContainerType();

    ZonedDateTime getCreatedTime();

    // ===== import =====
    String getImportDeclNo();
    String getDeliveryOrderLocation();
    String getBlNo();

    // ===== export =====
    String getExportDeclNo();
    String getBookingNo();
    String getSoNo();
}
