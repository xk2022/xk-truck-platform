package com.xk.truck.fms.controller.api.dto;

import com.xk.truck.fms.domain.model.DriverLicenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverRegisterReq {

    @NotBlank
    private String name;

    @NotBlank
    private String phone; // 將會成為登入帳號

    @NotNull
    private DriverLicenseType licenseType;
}
