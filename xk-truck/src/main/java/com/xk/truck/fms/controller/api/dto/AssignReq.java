package com.xk.truck.fms.controller.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignReq {
    @NotNull
    private UUID orderId;
    @NotNull
    private UUID vehicleId;
    @NotNull
    private UUID driverId;
}
