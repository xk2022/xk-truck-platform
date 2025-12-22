package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.UpmsPermissionService;
import com.xk.truck.upms.controller.api.dto.permission.*;

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
 * Controller Class : UpmsPermissionController
 * Layer            : Controller (API)
 * Purpose          : 提供權限管理 API（建立/查詢/分頁/啟用停用/更新/刪除）
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
@Tag(name = "UPMS - Permission API", description = "權限管理相關操作（建立、查詢、啟用、停用、更新、刪除）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upms/permissions")
public class UpmsPermissionController {

    private final UpmsPermissionService permissionService;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立權限
     * POST /api/upms/permissions
     */
    @Operation(summary = "建立權限")
    @PostMapping
    public ApiResult<UpmsPermissionResp> create(@Valid @RequestBody UpmsPermissionCreateReq req) {
        return ApiResult.success(permissionService.create(req));
    }

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 取得單一權限（依 UUID）
     * GET /api/upms/permissions/{id}
     */
    @Operation(summary = "取得單一權限（依 UUID）", description = "依 UUID 查詢指定權限")
    @GetMapping("/{id}")
    public ApiResult<UpmsPermissionResp> findById(@PathVariable("id") UUID id) {
        return ApiResult.success(permissionService.findById(id));
    }

    /**
     * 權限列表分頁查詢
     * GET /api/upms/permissions
     * <p>
     * query 來源：request params 綁定到 UpmsPermissionQuery
     * pageable 來源：?page=0&size=20&sort=createdTime,desc
     */
    @Operation(summary = "查詢權限列表（支援分頁 + 條件查詢）")
    @GetMapping
    public ApiResult<Page<UpmsPermissionListResp>> pageForList(
            @ParameterObject @ModelAttribute UpmsPermissionQuery query,
            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
    ) {
        return ApiResult.success(permissionService.pageForList(query, pageable));
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新權限基本資料（不改 code）
     * PATCH /api/upms/permissions/{id}
     */
    @Operation(summary = "更新權限基本資料（不改代碼）")
    @PatchMapping("/{id}")
    public ApiResult<UpmsPermissionResp> updateBasic(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsPermissionUpdateReq req
    ) {
        return ApiResult.success(permissionService.updateBasic(id, req));
    }

    // ===============================================================
    // Status operations
    // ===============================================================

    /**
     * 啟用 / 停用
     * PATCH /api/upms/permissions/{id}/enabled?enabled=true|false
     */
    @Operation(summary = "啟用或停用權限", description = "切換權限啟用狀態")
    @PatchMapping("/{id}/enabled")
    public ApiResult<Void> updateEnabled(
            @PathVariable("id") UUID id,
            @RequestParam("enabled") boolean enabled
    ) {
        permissionService.updateEnabled(id, enabled);
        return ApiResult.success();
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除權限
     * DELETE /api/upms/permissions/{id}
     */
    @Operation(summary = "刪除權限", description = "刪除指定權限（MVP 版本採硬刪除）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") UUID id) {
        permissionService.delete(id);
        return ApiResult.success();
    }
}
