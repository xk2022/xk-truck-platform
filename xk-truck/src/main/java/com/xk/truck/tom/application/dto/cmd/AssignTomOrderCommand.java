package com.xk.truck.tom.application.dto.cmd;

import lombok.Data;

import java.util.UUID;

@Data
public class AssignTomOrderCommand {
    private UUID orderUuid;

    // 方案：之後你派遣會需要 driver/vehicle
    private UUID driverUuid;
    private UUID vehicleUuid;
}
