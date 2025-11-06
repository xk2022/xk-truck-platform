package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.VehicleStatus;

import com.xk.truck.fms.domain.model.VehicleType;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class VehicleResp {
    private UUID id;
    private String plateNo;
    private VehicleType type;
    private VehicleStatus status;
    private String brand;
    private String model;
    private Double capacityTon;
    private UUID currentDriverId;
    private Instant createdTime;
    private Instant updatedTime;
}
