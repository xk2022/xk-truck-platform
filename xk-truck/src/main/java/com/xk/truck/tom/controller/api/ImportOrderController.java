package com.xk.truck.tom.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.tom.application.mapper.ImportOrderMapper;
import com.xk.truck.tom.application.usecase.CreateImportOrderUseCase;
import com.xk.truck.tom.application.usecase.cmd.CreateImportOrderCmd;
import com.xk.truck.tom.application.usecase.dto.OrderResp;
import com.xk.truck.tom.controller.api.dto.CreateImportOrderReq;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ===============================================================
 * Controller Class : ImportOrderController
 * Layer            : Controller (API)
 * Purpose          : 提供使用者管理 API (CRUD、啟用停用、重設密碼、角色指派)
 * Notes            :
 * - 回傳 ApiResult，統一回應格式
 * - 目前回傳 Entity（MVP）；未來可替換為 DTO
 * <p>
 * Controller 設計原則（對齊 Service 風格）
 * 1) Controller 不做業務邏輯：不 encode 密碼、不做 exists 檢查、不操作關聯
 * 2) 例外與規則統一由 Service 處理（BusinessException）
 * 3) DTO 驗證交給 @Valid / @Validated（若 DTO 尚未加註解，先保留入口）
 * 4) Query / Pageable 明確分工：query 用 request params 綁定、pageable 用 Spring Data
 * ===============================================================
 */
@Tag(name = "TOM - Order Import API", description = "進口訂單")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tom/orders/import")
public class ImportOrderController {

    private final CreateImportOrderUseCase createImportOrderUseCase;
    private final ImportOrderMapper importOrderMapper;

    // ===============================================================
    // Create
    // ===============================================================

    @Operation(summary = "建立進口訂單")
    @PostMapping
    public ApiResult<OrderResp> create(@RequestBody CreateImportOrderReq req) {
        log.info("[API] createImport request={}", req);

        CreateImportOrderCmd cmd = importOrderMapper.toCmd(req);
        OrderResp resp = createImportOrderUseCase.execute(cmd);
        return ApiResult.success(resp, "進口訂單建立成功");
    }
}
