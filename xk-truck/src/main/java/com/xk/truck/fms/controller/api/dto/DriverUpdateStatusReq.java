package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.DriverStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 專用：更新司機狀態
 */
@Data
public class DriverUpdateStatusReq {
    @NotNull
    private DriverStatus status;
}
