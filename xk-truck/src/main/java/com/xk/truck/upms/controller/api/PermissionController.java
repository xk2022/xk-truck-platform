package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.controller.api.dto.permission.*;
import com.xk.truck.upms.domain.service.PermissionService;

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

import java.util.*;

/**
 * ===============================================================
 * Controller Class : PermissionController
 * Layer            : API (REST Controller)
 * Purpose          : 提供權限管理 CRUD API 端點
 * ===============================================================
 */
@Tag(name = "UPMS - Permission API", description = "權限管理相關操作（建立、查詢、更新、刪除）")
@Slf4j
@RestController
@RequestMapping("/api/upms/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "建立權限", description = "建立一個新權限項目")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResult<PermissionResp> create(@RequestBody PermissionCreateReq req) {
        return ApiResult.success(permissionService.create(req), "權限建立成功");
    }

    @Operation(summary = "查詢權限列表（支援分頁）", description = "回傳權限清單")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCH')")
    @GetMapping
    public ApiResult<Page<PermissionResp>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
        Page<PermissionResp> result = permissionService.list(pageable);
        return ApiResult.success(result, "查詢成功");
    }

    @Operation(summary = "取得單一權限", description = "依 UUID 查詢指定權限")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<PermissionResp>> get(@PathVariable UUID id) {
        PermissionResp perm = permissionService.findById(id);
        return ResponseEntity.ok(ApiResult.success(perm, "查詢成功"));
    }

    @Operation(summary = "更新權限", description = "更新權限名稱或描述")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<PermissionResp>> update(@PathVariable UUID id,
                                                            @RequestBody PermissionUpdateReq req) {
        PermissionResp updated = permissionService.update(id, req);
        return ResponseEntity.ok(ApiResult.success(updated, "更新成功"));
    }

    @Operation(summary = "刪除權限", description = "刪除指定權限（MVP 為硬刪除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable UUID id) {
        permissionService.delete(id);
        return ApiResult.success(null, "權限已刪除");
    }
}
