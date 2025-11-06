package com.xk.truck.fms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.fms.application.usecase.DriverRegisterUseCase;
import com.xk.truck.fms.controller.api.dto.DriverRegisterReq;
import com.xk.truck.fms.controller.api.dto.DriverResp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FMS - Driver 司機建立流程（連動 User & Role）")
@RestController
@RequestMapping("/api/fms/driver-register")
@RequiredArgsConstructor
@Validated
public class DriverRegisterController {

    private final DriverRegisterUseCase useCase;

    @Operation(summary = "建立司機（自動建立 User + 指派 ROLE_DRIVER + 建立 Driver）")
    @PostMapping
    public ApiResult<DriverResp> register(@RequestBody DriverRegisterReq req) {
        return ApiResult.success(useCase.register(req), "司機建立完成");
    }
}
