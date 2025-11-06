package com.xk.truck.fms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.fms.controller.api.dto.*;
import com.xk.truck.fms.domain.service.DriverService;

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

@Tag(name = "FMS - Driver 司機管理")
@RestController
@RequestMapping("/api/fms/drivers")
@RequiredArgsConstructor
@Validated
public class DriverController {

    private final DriverService service;

    @Operation(summary = "建立司機")
    @PostMapping
    public ApiResult<DriverResp> create(@Valid @RequestBody DriverCreateReq req) {
        return ApiResult.success(service.create(req), "建立成功");
    }

    @Operation(summary = "取得司機詳情")
    @GetMapping("/{id}")
    public ApiResult<DriverResp> findById(@PathVariable UUID id) {
        return ApiResult.success(service.findById(id));
    }

    @Operation(summary = "司機分頁查詢")
    @GetMapping
    public ApiResult<Page<DriverResp>> list(
            @PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResult.success(service.list(pageable));
    }

    @Operation(summary = "更新司機（全量更新可變更欄位）")
    @PutMapping("/{id}")
    public ApiResult<DriverResp> update(@PathVariable UUID id,
                                        @Valid @RequestBody DriverCreateReq req) {
        return ApiResult.success(service.update(id, req), "更新成功");
    }

    @Operation(summary = "更新司機狀態（僅 status）")
    @PatchMapping("/{id}/status")
    public ApiResult<DriverResp> updateStatus(@PathVariable UUID id,
                                              @Valid @RequestBody DriverUpdateStatusReq req) {
        return ApiResult.success(service.updateStatus(id, req.getStatus()), "狀態更新成功");
    }

    @Operation(summary = "更新司機上線狀態（僅 onDuty）")
    @PatchMapping("/{id}/on-duty")
    public ApiResult<DriverResp> updateOnDuty(@PathVariable UUID id,
                                              @Valid @RequestBody DriverUpdateOnDutyReq req) {
        return ApiResult.success(service.updateOnDuty(id, req.getOnDuty()), "上線狀態已更新");
    }

    @Operation(summary = "刪除司機")
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable UUID id) {
        boolean removed = service.delete(id);
        return ApiResult.success(removed, removed ? "刪除成功" : "未找到該司機");
    }
}
