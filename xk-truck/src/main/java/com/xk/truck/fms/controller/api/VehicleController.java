package com.xk.truck.fms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.fms.controller.api.dto.UpdateStatusReq;
import com.xk.truck.fms.controller.api.dto.VehicleCreateReq;
import com.xk.truck.fms.controller.api.dto.VehicleResp;
import com.xk.truck.fms.domain.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "FMS - Vehicle 車輛管理")
@RestController
@RequestMapping("/api/fms/vehicles")
@RequiredArgsConstructor
@Validated
public class VehicleController {

    private final VehicleService service;

    @Operation(summary = "建立車輛")
    @PostMapping
    public ApiResult<VehicleResp> create(@Valid @RequestBody VehicleCreateReq req) {
        return ApiResult.success(service.create(req), "建立成功");
    }

    @Operation(summary = "取得車輛詳情")
    @GetMapping("/{id}")
    public ApiResult<VehicleResp> findById(@PathVariable UUID id) {
        return ApiResult.success(service.findById(id));
    }

    @Operation(summary = "車輛分頁查詢")
    @GetMapping
    public ApiResult<Page<VehicleResp>> list(
            @PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResult.success(service.list(pageable));
    }

    @Operation(summary = "更新車輛（全量更新可變更欄位）")
    @PutMapping("/{id}")
    public ApiResult<VehicleResp> update(@PathVariable UUID id,
                                         @Valid @RequestBody VehicleCreateReq req) {
        return ApiResult.success(service.update(id, req), "更新成功");
    }

    @Operation(summary = "更新車輛狀態（僅更新 status）")
    @PatchMapping("/{id}/status")
    public ApiResult<VehicleResp> updateStatus(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateStatusReq req) {
        return ApiResult.success(service.updateStatus(id, req.getStatus()), "狀態更新成功");
    }

    @Operation(summary = "刪除車輛")
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable UUID id) {
        boolean removed = service.delete(id);
        return ApiResult.success(removed, removed ? "刪除成功" : "未找到該車輛");
    }
}
