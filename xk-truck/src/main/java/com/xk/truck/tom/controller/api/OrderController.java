package com.xk.truck.tom.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.tom.application.mapper.ImportOrderMapper;
import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.CreateImportOrderUseCase;
import com.xk.truck.tom.application.usecase.FindOrderUseCase;

import com.xk.truck.tom.application.usecase.GetOrderDetailUseCase;
import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.dto.OrderListItemResp;

import com.xk.truck.tom.controller.api.dto.AssignOrderReq;
import com.xk.truck.tom.controller.api.dto.OrderQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : OrderController
 * Layer            : Controller (API)
 * Purpose          : 提供 TOM 訂單管理 API（Create / List / Detail / Assign / Status）
 * Notes            :
 * - 回傳 ApiResult，統一回應格式
 * - DDD 分層：Controller → UseCase → DomainService → Repository → JPA
 * <p>
 * Controller 設計原則（對齊 UpmsUserController 風格）
 * 1) Controller 不做業務邏輯：不做 exists 檢查、不操作關聯、不寫入狀態歷程
 * 2) 規則/例外統一由 UseCase/Domain 處理（BusinessException）
 * 3) DTO 驗證交給 @Valid / @Validated
 * 4) Query / Pageable 明確分工：query 用 request params 綁定、pageable 用 Spring Data
 * ===============================================================
 */
@Tag(name = "TOM - Order API", description = "訂單管理相關操作（建立、查詢、派單、狀態等）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tom/orders")
public class OrderController {

    // ===============================================================
    // UseCases (Application Layer)
    // ===============================================================
    private final CreateImportOrderUseCase createImportOrderUseCase;
//    private final CreateExportOrderUseCase createExportOrderUseCase;

    private final FindOrderUseCase findOrderUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;

//    private final AssignOrderUseCase assignOrderUseCase;

    private final OrderMapper orderMapper;
    private final ImportOrderMapper importOrderMapper;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立進口訂單
     * POST /api/tom/orders/import
     */
//    @Operation(summary = "建立進口訂單（IMPORT）")
//    @PostMapping("/import")
//    public ApiResult<OrderResp> createImport(@Valid @RequestBody CreateImportOrderReq req) {
//        log.info("[API] createImport request={}", req);
//        var cmd = importOrderMapper.toCmd(req);
//        var resp = createImportOrderUseCase.execute(cmd);
//        return ApiResult.success(resp, "建立進口訂單成功");
//    }

    /**
     * 建立出口訂單
     * POST /api/tom/orders/export
     */
//    @Operation(summary = "建立出口訂單（EXPORT）")
//    @PostMapping("/export")
//    public ApiResult<OrderResp> createExport(@Valid @RequestBody CreateExportOrderReq req) {
//        log.info("[API] createExport request={}", req);
//        return ApiResult.success(createExportOrderUseCase.execute(req), "建立出口訂單成功");
//    }

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 訂單列表（同時回傳 import/export 需要的欄位）
     * GET /api/tom/orders?page=0&size=20&sort=createdTime,desc
     * <p>
     * - 使用 Projection DTO（Row/Projection）避免 JPA N+1 / lazy 踩坑
     * - query 來源：request params（Spring 會自動綁定到 OrderQuery）
     * - pageable 來源：?page=0&size=20&sort=createdTime,desc
     */
    @Operation(summary = "訂單列表（同時回 import/export，Projection DTO）")
    @GetMapping
    public ApiResult<Page<OrderListItemResp>> pageForList(
            @ParameterObject @ModelAttribute OrderQuery query,
            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
    ) {
        var qry = orderMapper.toQry(query);
        var result = findOrderUseCase.pageForList(qry, pageable);
        return ApiResult.success(result);
    }

    /**
     * 訂單明細（依 UUID）
     * GET /api/tom/orders/{id}
     */
    @Operation(summary = "取得訂單明細（依 UUID）")
    @GetMapping("/{id}")
    public ApiResult<OrderDetailResp> findById(@PathVariable("id") UUID id) {
        log.info("[API] getOrderDetail id={}", id);
        return ApiResult.success(getOrderDetailUseCase.findById(id));
    }

    // ===============================================================
    // Dispatch / Assignment
    // ===============================================================

    /**
     * 派單（指派車輛/司機）
     * POST /api/tom/orders/{id}/assign
     * <p>
     * 規則建議（由 UseCase/DomainService 實作）：
     * - 訂單狀態必須允許派單（CREATED/CONFIRMED...）
     * - 允許改派：寫入 assignment history、同步主表 vehicleUuid/driverUuid
     * - 自動寫入狀態歷程（例如 ASSIGNED）
     */
    @Operation(summary = "派單（指派車輛/司機）") @PostMapping("/{id}/assign")
    public ApiResult<Void> assign(@PathVariable("id") UUID id, @Valid @RequestBody AssignOrderReq req) {
//        assignOrderUseCase.assign(id, req);
        return ApiResult.success();
    }

    // ===============================================================
    // Status operations (Optional for MVP)
    // ===============================================================

    /**
     * 變更狀態（可選）
     * PATCH /api/tom/orders/{id}/status?status=...
     *
     * 若你偏好更一致的 DTO 風格，可改成 @RequestBody ChangeOrderStatusReq
     */
    // @Operation(summary = "更新訂單狀態")
    // @PatchMapping("/{id}/status")
    // public ApiResult<Void> updateStatus(
    //         @PathVariable("id") UUID id,
    //         @RequestParam("status") OrderStatus status,
    //         @RequestParam(value = "reason", required = false) String reason
    // ) {
    //     updateOrderStatusUseCase.update(id, status, reason);
    //     return ApiResult.success();
    // }

    // ===============================================================
    // Delete (Optional for MVP)
    // ===============================================================

    /**
     * 刪除訂單（可選）
     * DELETE /api/tom/orders/{id}
     *
     * 建議：真實物流單通常採「取消/作廢」而非硬刪除
     */
    // @Operation(summary = "刪除訂單（MVP 可用硬刪除；建議改取消）")
    // @DeleteMapping("/{id}")
    // public ApiResult<Void> delete(@PathVariable("id") UUID id) {
    //     deleteOrderUseCase.delete(id);
    //     return ApiResult.success();
    // }
}
