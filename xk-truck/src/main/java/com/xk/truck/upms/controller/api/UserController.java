package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.controller.api.dto.user.*;
import com.xk.truck.upms.domain.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ===============================================================
 * Controller : UserController
 * Layer      : API (REST)
 * Purpose    : 提供使用者管理 API (CRUD、啟用停用、重設密碼、角色指派)
 * Notes      :
 * - 回傳 ApiResult，統一回應格式
 * - 目前回傳 Entity（MVP）；未來可替換為 DTO
 * ===============================================================
 */
@Tag(name = "UPMS - User API", description = "使用者管理相關操作（建立、查詢、啟用、停用、重設密碼等）")
@Slf4j
@RestController
@RequestMapping("/api/upms/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "建立使用者")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResult<UserResp> create(@RequestBody UserCreateReq req) {
        return ApiResult.success(userService.create(req));
    }

    @Operation(summary = "查詢使用者列表（支援分頁）")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCH')")
    @GetMapping
    public ApiResult<Page<UserResp>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
        Page<UserResp> result = userService.list(pageable);
        return ApiResult.success(result);
    }

    @Operation(summary = "取得單一使用者", description = "依 UUID 查詢指定使用者")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<UserResp>> get(@PathVariable UUID id) {
        UserResp user = userService.findById(id);
        return ResponseEntity.ok(ApiResult.success(user));
    }

    @Operation(summary = "更新使用者")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResult<UserResp> update(@PathVariable UUID id, @RequestBody UserUpdateReq req) {
        return ApiResult.success(userService.update(id, req));
    }

    @Operation(summary = "啟用或停用帳號", description = "切換使用者啟用狀態")
    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResult<UserResp>> enable(@PathVariable UUID id,
                                                      @RequestParam boolean enabled) {
        UserResp result = userService.enable(id, enabled);
        String msg = enabled ? "使用者已啟用" : "使用者已停用";
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(summary = "重設密碼", description = "管理員可為使用者重設新密碼")
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResult<Void>> resetPassword(@PathVariable UUID id,
                                                         @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(summary = "刪除使用者", description = "刪除指定使用者（MVP 版本採硬刪除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ApiResult.success(null);
    }
}
