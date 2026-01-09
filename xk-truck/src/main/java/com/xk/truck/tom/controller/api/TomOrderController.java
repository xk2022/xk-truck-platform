package com.xk.truck.tom.controller.api;

import com.xk.base.web.ApiResult;

import com.xk.truck.tom.application.port.in.CreateTomOrderUseCase;
import com.xk.truck.tom.controller.api.mapper.TomOrderApiMapper;
import com.xk.truck.tom.application.port.in.FindTomOrderUseCase;

import com.xk.truck.tom.controller.api.dto.req.CreateTomOrderReq;

import com.xk.truck.tom.controller.api.dto.req.TomOrderQuery;
import com.xk.truck.tom.controller.api.dto.resp.TomOrderResp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : OrderController
 * Layer            : Interface Adapters (RestController) 處理 HTTP 請求
 * Purpose          : 提供 TOM 訂單管理 API（Create / List / Detail / Assign / Status）
 * ===============================================================
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tom/orders")
public class TomOrderController {

    // ===============================================================
    // UseCases (Application Layer)
    // ===============================================================

    private final TomOrderApiMapper mapper;

    private final CreateTomOrderUseCase createTomOrderUseCase;

    // ===============================================================
    // Create
    // ===============================================================

    @Operation(summary = "建立訂單")
    @PostMapping
    public ApiResult<TomOrderResp> create(@Valid @RequestBody CreateTomOrderReq req) {
        log.info("[API] create request={}", req);
        var cmd = mapper.toCreateCmd(req);
        var result = createTomOrderUseCase.execute(cmd);
        return ApiResult.success(mapper.toResp(result), "建立進口訂單成功");
    }

    // ===============================================================
    // Read
    // ===============================================================

//    @Operation(summary = "取得訂單明細（依 UUID）")
//    @GetMapping("/{id}")
//    public ApiResult<TomOrderResp> findById(@PathVariable("id") UUID id) {
//        log.info("[API] getOrderDetail id={}", id);
//        return ApiResult.success(findUseCase.findById(id));
//    }


    /**
     * 訂單列表（同時回傳 import/export 需要的欄位）
     * GET /api/tom/orders?page=0&size=20&sort=createdTime,desc
     * <p>
     * - 使用 Projection DTO（Row/Projection）避免 JPA N+1 / lazy 踩坑
     * - query 來源：request params（Spring 會自動綁定到 OrderQuery）
     * - pageable 來源：?page=0&size=20&sort=createdTime,desc
     */
//    @Operation(summary = "訂單列表（同時回 import/export，Projection DTO）")
//    @GetMapping
//    public ApiResult<Page<TomOrderResp>> pageForList(
//            @ParameterObject @ModelAttribute TomOrderQuery query,
//            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
//    ) {
//        var qry = mapper.toQry(query);
//        var result = findUseCase.pageForList(qry, pageable);
//        return ApiResult.success(result);
//    }

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
//    @Operation(summary = "派單（指派車輛/司機）") @PostMapping("/{id}/assign")
//    public ApiResult<Void> assign(@PathVariable("id") UUID id, @Valid @RequestBody AssignOrderReq req) {
////        assignOrderUseCase.assign(id, req);
//        return ApiResult.success();
//    }

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
