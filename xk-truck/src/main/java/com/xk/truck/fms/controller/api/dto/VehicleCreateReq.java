package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.VehicleType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleCreateReq {

    @Schema(example = "ABC-1234")
    @NotBlank
    private String plateNo;

    @NotNull
    private VehicleType type;

    private String brand;

    private String model;

    private Double capacityTon;
}
