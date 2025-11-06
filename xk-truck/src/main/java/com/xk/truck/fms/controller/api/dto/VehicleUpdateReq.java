package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.VehicleStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.xk.truck.fms.domain.model.VehicleType;

@Data
public class VehicleUpdateReq {

    @NotBlank
    private String plateNo;

    @NotNull
    private VehicleType type;

    @NotNull
    private VehicleStatus status;

    private String brand;

    private String model;

    private Double capacityTon;
}
