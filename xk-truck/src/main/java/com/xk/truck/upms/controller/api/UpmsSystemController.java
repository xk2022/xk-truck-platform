package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.UpmsSystemService;
import com.xk.truck.upms.controller.api.dto.system.*;

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
 * Controller Class : UpmsSystemController
 * Layer            : Controller (API)
 * Purpose          : 提供系統管理 API（建立/查詢/分頁/啟用停用/更新/刪除）
 * Notes            :
 * - 回傳 ApiResult，統一回應格式
 * <p>
 * Controller 設計原則（對齊 Service 風格）
 * 1) Controller 不做業務邏輯：不做 exists 檢查、不做 normalize、不操作關聯
 * 2) 例外與規則統一由 Service 處理（BusinessException）
 * 3) DTO 驗證交給 @Valid / @Validated
 * 4) Query / Pageable 明確分工：query 用 request params 綁定、pageable 用 Spring Data
 * ===============================================================
 */
@Tag(name = "UPMS - System API", description = "系統管理相關操作（建立、查詢、更新、刪除、啟用停用）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upms/systems")
public class UpmsSystemController {

    private final UpmsSystemService systemService;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立系統
     * POST /api/upms/systems
     */
    @Operation(summary = "建立系統")
    @PostMapping
    public ApiResult<UpmsSystemResp> create(@Valid @RequestBody UpmsSystemCreateReq req) {
        return ApiResult.success(systemService.create(req));
    }

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 取得單一系統（依 UUID）
     * GET /api/upms/systems/{id}
     */
    @Operation(summary = "取得單一系統（依 UUID）", description = "依 UUID 查詢指定系統")
    @GetMapping("/{id}")
    public ApiResult<UpmsSystemResp> findById(@PathVariable("id") UUID id) {
        return ApiResult.success(systemService.findById(id));
    }

    /**
     * 系統列表分頁查詢
     * GET /api/upms/systems
     * <p>
     * query 來源：request params 綁定到 UpmsSystemQuery
     * pageable 來源：?page=0&size=20&sort=createdTime,desc
     */
    @Operation(summary = "查詢系統列表（支援分頁 + 條件查詢）")
    @GetMapping
    public ApiResult<Page<UpmsSystemListResp>> pageForList(
            @ParameterObject @ModelAttribute UpmsSystemQuery query,
            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
    ) {
        return ApiResult.success(systemService.pageForList(query, pageable));
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新系統基本資料
     * PATCH /api/upms/systems/{id}
     */
    @Operation(summary = "更新系統基本資料")
    @PatchMapping("/{id}")
    public ApiResult<UpmsSystemResp> updateBasic(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsSystemUpdateReq req
    ) {
        return ApiResult.success(systemService.updateBasic(id, req));
    }

    // ===============================================================
    // Status operations
    // ===============================================================

    /**
     * 啟用/停用
     * PATCH /api/upms/systems/{id}/enabled?enabled=true|false
     */
    @Operation(summary = "啟用或停用系統", description = "切換系統啟用狀態")
    @PatchMapping("/{id}/enabled")
    public ApiResult<Void> updateEnabled(
            @PathVariable("id") UUID id,
            @RequestParam("enabled") boolean enabled
    ) {
        systemService.updateEnabled(id, enabled);
        return ApiResult.success();
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除系統
     * DELETE /api/upms/systems/{id}
     */
    @Operation(summary = "刪除系統", description = "刪除指定系統（MVP 版本採硬刪除）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") UUID id) {
        systemService.delete(id);
        return ApiResult.success();
    }
}
