package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.DriverLicenseType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 建立/修改 Driver 的請求 DTO（共用）
 */
@Data
public class DriverCreateReq {

    @NotBlank
    @Schema(description = "姓名", example = "王小明")
    private String name;

    @NotBlank
    @Schema(description = "電話(唯一)", example = "0912000123")
    private String phone;

    @NotNull
    @Schema(description = "駕照類型", example = "LARGE")
    private DriverLicenseType licenseType;
}
