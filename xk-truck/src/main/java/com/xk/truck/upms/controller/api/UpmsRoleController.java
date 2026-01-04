package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.UpmsRoleService;
import com.xk.truck.upms.controller.api.dto.role.*;

import com.xk.truck.upms.controller.api.dto.system.UpmsSystemQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : UpmsRoleController
 * Layer            : Controller (API)
 * Purpose          : 提供角色管理 API（CRUD、分頁查詢、啟用停用、下拉選項）
 * Notes            :
 * - 回傳 ApiResult，統一回應格式
 * - Controller 不做業務邏輯：不做 exists 檢查、不處理中介表關聯（交給 Service）
 * - DTO 驗證交給 @Valid / @Validated
 * - Query / Pageable：query 由 request params 綁定、pageable 由 Spring Data 處理
 * ===============================================================
 */
@Tag(name = "UPMS - Role API", description = "角色管理相關操作（建立、查詢、更新、啟用停用、刪除、下拉選項等）")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upms/roles")
public class UpmsRoleController {

    private final UpmsRoleService roleService;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立角色（可選：綁定 permissions）
     * POST /api/upms/roles
     */
    @Operation(summary = "建立角色（可綁定權限）")
    @PostMapping
    public ApiResult<UpmsRoleResp> create(@Valid @RequestBody UpmsRoleCreateReq req) {
        return ApiResult.success(roleService.create(req));
    }

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 分頁查詢角色列表（支援條件查詢）
     * GET /api/upms/roles
     * <p>
     * query 來源：request params（Spring 會自動綁定到 UpmsRoleQuery）
     * pageable 來源：?page=0&size=20&sort=createdTime,desc
     */
    @Operation(summary = "查詢角色列表（支援分頁 + 條件查詢）")
    @GetMapping
    public ApiResult<Page<UpmsRoleListResp>> pageForList(
            @ParameterObject @ModelAttribute UpmsRoleQuery query,
            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
    ) {
        return ApiResult.success(roleService.pageForList(query, pageable));
    }

    /**
     * 取得單一角色
     * GET /api/upms/roles/{id}
     */
    @Operation(summary = "取得單一角色", description = "依 UUID 查詢指定角色")
    @GetMapping("/{id}")
    public ApiResult<UpmsRoleResp> findById(@PathVariable("id") UUID id) {
        return ApiResult.success(roleService.findById(id));
    }

    /**
     * 角色下拉選項（啟用中）
     * GET /api/upms/roles/options
     * <p>
     * ⚠ 若你目前 service 回傳的是 List<RoleOptionResp> 就維持這個型別
     * 若你實際已改名為 UpmsRoleOptionResp，請把下方型別一併替換。
     */
    @Operation(summary = "角色下拉選項（啟用中）")
    @GetMapping("/options")
    public ApiResult<List<UpmsRoleOptionResp>> options() {
        return ApiResult.success(roleService.options());
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新角色基本資料（不含 permission 指派）
     * PATCH /api/upms/roles/{id}
     */
    @Operation(summary = "更新角色基本資料（不含權限綁定）")
    @PatchMapping("/{id}")
    public ApiResult<UpmsRoleResp> updateBasic(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsRoleUpdateReq req
    ) {
        return ApiResult.success(roleService.updateBasic(id, req));
    }

    /**
     * 覆蓋式更新（基本 + permissions）
     * PUT /api/upms/roles/{id}
     * <p>
     * 規則（由 Service 實作）：
     * - req.permissionCodes == null → 不動 permissions
     * - req.permissionCodes != null → replacePermissions
     */
    @Operation(summary = "更新角色（含權限綁定）")
    @PutMapping("/{id}")
    public ApiResult<UpmsRoleResp> updateAll(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsRoleUpdateReq req
    ) {
        return ApiResult.success(roleService.updateAll(id, req));
    }

    // ===============================================================
    // Status operations
    // ===============================================================

    /**
     * 啟用/停用角色
     * PATCH /api/upms/roles/{id}/enabled?enabled=true|false
     */
    @Operation(summary = "啟用或停用角色", description = "切換角色啟用狀態")
    @PatchMapping("/{id}/enabled")
    public ApiResult<Void> updateEnabled(
            @PathVariable("id") UUID id,
            @RequestParam("enabled") boolean enabled
    ) {
        roleService.updateEnabled(id, enabled);
        return ApiResult.success();
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除角色（MVP：硬刪除）
     * DELETE /api/upms/roles/{id}
     */
    @Operation(summary = "刪除角色", description = "刪除指定角色（MVP 版本採硬刪除）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") UUID id) {
        roleService.delete(id);
        return ApiResult.success();
    }
}
