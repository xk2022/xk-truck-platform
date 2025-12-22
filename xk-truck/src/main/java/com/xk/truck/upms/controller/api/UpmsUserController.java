package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.UpmsUserService;
import com.xk.truck.upms.controller.api.dto.user.*;

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
 * Controller Class : UpmsUserController
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
@Tag(name = "UPMS - User API", description = "使用者管理相關操作（建立、查詢、啟用、停用、重設密碼等）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upms/users")
public class UpmsUserController {

    private final UpmsUserService userService;

    // ===============================================================
    // Create
    // ===============================================================

    @Operation(summary = "建立使用者（含角色）")
    @PostMapping
    public ApiResult<UpmsUserResp> create(@Valid @RequestBody UpmsUserCreateReq req) {
        return ApiResult.success(userService.create(req));
    }

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 依 UUID 查詢
     * GET /api/upms/users/{id}
     */
    @Operation(summary = "取得單一使用者（依 UUID）", description = "依 UUID 查詢指定使用者")
    @GetMapping("/{id}")
    public ApiResult<UpmsUserResp> findById(@PathVariable("id") UUID id) {
        return ApiResult.success(userService.findById(id));
    }

    /**
     * 依 username 查詢
     * GET /api/upms/users/by-username/{username}
     * <p>
     * 註：若你不喜歡 path 版本，也可改成：
     * GET /api/upms/users/by-username?username=xxx
     */
    @Operation(summary = "取得單一使用者（依帳號）", description = "依 username 查詢指定使用者")
    @GetMapping("/by-username/{username}")
    public ApiResult<UpmsUserResp> findByUsername(@PathVariable("username") String username) {
        return ApiResult.success(userService.findByUsername(username));
    }

    /**
     * 後台列表分頁查詢
     * GET /api/upms/users
     * <p>
     * query 來源：request params（Spring 會自動綁定到 UpmsUserQuery）
     * pageable 來源：?page=0&size=20&sort=username,asc
     */
    @Operation(summary = "查詢使用者列表（支援分頁 + 條件查詢）")
    @GetMapping
    public ApiResult<Page<UpmsUserListResp>> pageForList(
            @ParameterObject @ModelAttribute UpmsUserQuery query,
            @ParameterObject @PageableDefault(size = 20, sort = "createdTime") Pageable pageable
    ) {
        Page<UpmsUserListResp> result = userService.pageForList(query, pageable);
        return ApiResult.success(result);
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新基本資料（不含角色/不含密碼）
     * PATCH /api/upms/users/{id}
     */
    @Operation(summary = "更新使用者基本資料（不含角色/不含密碼）")
    @PatchMapping("/{id}")
    public ApiResult<UpmsUserResp> updateBasic(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsUserUpdateReq req
    ) {
        return ApiResult.success(userService.updateBasic(id, req));
    }

    /**
     * 覆蓋式更新（基本 + 角色）
     * PUT /api/upms/users/{id}
     * <p>
     * 規則（由 Service 實作）：
     * - req.roleCodes != null → replaceRoles
     * - req.roleCodes == null → 不動角色
     */
    @Operation(summary = "更新使用者")
    @PutMapping("/{id}")
    public ApiResult<UpmsUserResp> updateAll(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsUserUpdateReq req
    ) {
        return ApiResult.success(userService.updateAll(id, req));
    }

    // ===============================================================
    // Security operations
    // ===============================================================

    /**
     * 啟用/停用
     * PATCH /api/upms/users/{id}/enabled?enabled=true|false
     */
    @Operation(summary = "啟用或停用帳號", description = "切換使用者啟用狀態")
    @PatchMapping("/{id}/enabled")
    public ApiResult<Void> updateEnabled(
            @PathVariable("id") UUID id,
            @RequestParam("enabled") boolean enabled
    ) {
        userService.updateEnabled(id, enabled);
        return ApiResult.success();
    }

    /**
     * 重設密碼
     * PATCH /api/upms/users/{id}/password
     * <p>
     * 建議用 DTO：避免直接傳 String，未來好擴充（例如：強度、強制登出、到期時間）
     */
    @Operation(summary = "重設密碼", description = "管理員可為使用者重設新密碼")
    @PatchMapping("/{id}/password")
    public ApiResult<Void> resetPassword(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpmsUserResetPasswordReq req
    ) {
        userService.resetPassword(id, req.getNewPassword());
        return ApiResult.success();
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除使用者
     * DELETE /api/upms/users/{id}
     */
    @Operation(summary = "刪除使用者", description = "刪除指定使用者（MVP 版本採硬刪除）")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") UUID id) {
        userService.delete(id);
        return ApiResult.success();
    }
}
