package com.xk.truck.fms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.fms.controller.api.dto.AssignReq;
import com.xk.truck.fms.domain.model.DispatchTask;
import com.xk.truck.fms.domain.service.DispatchService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fms/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService service;

    @PostMapping("/assign")
    public ApiResult<DispatchTask> assign(@RequestBody AssignReq req) {
        return ApiResult.success(service.assign(req.getOrderId(), req.getVehicleId(), req.getDriverId()), "派工成功");
    }

    @PostMapping("/{id}/start")
    public ApiResult<DispatchTask> start(@PathVariable UUID id) {
        return ApiResult.success(service.start(id), "司機已出發");
    }

    @PostMapping("/{id}/sign")
    public ApiResult<DispatchTask> sign(@PathVariable UUID id) {
        return ApiResult.success(service.sign(id), "簽收完成（等待後台確認）");
    }

    @PostMapping("/{id}/complete")
    public ApiResult<DispatchTask> complete(@PathVariable UUID id) {
        return ApiResult.success(service.complete(id), "派工完成");
    }

    @PostMapping("/{id}/cancel")
    public ApiResult<DispatchTask> cancel(@PathVariable UUID id) {
        return ApiResult.success(service.cancel(id), "派工已取消");
    }
}
