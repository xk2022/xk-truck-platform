package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.DriverLicenseType;
import com.xk.truck.fms.domain.model.DriverStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 查詢/回傳用 DTO
 */
@Data
public class DriverResp {

    private UUID id;

    private String name;

    private String phone;

    private DriverLicenseType licenseType;

    private DriverStatus status;

    private boolean onDuty;

    private UUID currentVehicleId;

    @Schema(description = "建立時間")
    private ZonedDateTime createdTime;

    @Schema(description = "最後修改時間")
    private ZonedDateTime updatedTime;

    // 若你有需要 createdBy/updatedBy 也可加上
}
