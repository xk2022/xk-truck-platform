package com.xk.truck.tom.domain.model;

import lombok.Data;

/**
 * ===============================================================
 * Value Object : RouteInfo
 * Purpose      : 航線與時間資訊
 * ===============================================================
 */
@Data
public class RouteInfo {

    private String pickupAddress;
    private String deliveryAddress;
    private String pol;              // 裝貨港
    private String pod;              // 卸貨港
}
