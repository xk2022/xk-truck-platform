package com.xk.truck.fms.controller.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 專用：更新司機上線狀態
 */
@Data
public class DriverUpdateOnDutyReq {
    @NotNull
    private Boolean onDuty;
}
