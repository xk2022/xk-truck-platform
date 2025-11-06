package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.VehicleStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusReq {

    @NotNull
    private VehicleStatus status;
}
