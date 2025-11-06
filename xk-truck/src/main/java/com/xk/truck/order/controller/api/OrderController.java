package com.xk.truck.order.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.order.controller.api.dto.ChangeStatusReq;
import com.xk.truck.order.controller.api.dto.OrderCreateReq;
import com.xk.truck.order.controller.api.dto.OrderResp;
import com.xk.truck.order.domain.model.OrderStatus;
import com.xk.truck.order.domain.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "訂單管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @Operation(summary = "建立訂單")
    @PostMapping
    public ResponseEntity<ApiResult<OrderResp>> create(@Valid @RequestBody OrderCreateReq req) {
        return ResponseEntity.ok(ApiResult.success(service.create(req)));
    }

    @Operation(summary = "分頁查詢")
    @GetMapping
    public ApiResult<Page<OrderResp>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime,desc") String sort
    ) {
        var sp = sort.split(",");
        var s = Sort.by(Sort.Direction.fromString(sp.length > 1 ? sp[1] : "desc"), sp[0]);
        return ApiResult.success(service.page(page, size, s));
    }

    @Operation(summary = "查詢單筆")
    @GetMapping("/{uuid}")
    public ApiResult<OrderResp> get(@PathVariable String uuid) {
        return ApiResult.success(service.get(uuid));
    }

    @Operation(summary = "狀態流轉（通用）")
    @PatchMapping("/{uuid}/status")
    public ApiResult<OrderResp> change(@PathVariable String uuid, @Valid @RequestBody ChangeStatusReq req) {
        return ApiResult.success(service.changeStatus(uuid, req.getTo()));
    }

    // 方便前端、也可直接用 /status
    @PatchMapping("/{uuid}/accept") public ApiResult<OrderResp> accept(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.ACCEPTED));
    }

    @PatchMapping("/{uuid}/dispatch") public ApiResult<OrderResp> dispatch(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.DISPATCHED));
    }

    @PatchMapping("/{uuid}/pickup") public ApiResult<OrderResp> pickup(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.PICKED_UP));
    }

    @PatchMapping("/{uuid}/deliver") public ApiResult<OrderResp> deliver(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.DELIVERED));
    }

    @PatchMapping("/{uuid}/complete") public ApiResult<OrderResp> complete(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.COMPLETED));
    }

    @PatchMapping("/{uuid}/cancel") public ApiResult<OrderResp> cancel(@PathVariable String uuid) {
        return ApiResult.success(service.changeStatus(uuid, OrderStatus.CANCELED));
    }
}
