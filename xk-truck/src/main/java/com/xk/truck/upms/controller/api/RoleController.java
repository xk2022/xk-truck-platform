package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.controller.api.dto.role.*;
import com.xk.truck.upms.domain.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * Controller Class : RoleController
 * Layer            : API (REST Controller)
 * Purpose          : 提供角色管理 CRUD API 端點
 * ===============================================================
 */
@Tag(name = "UPMS - Role API", description = "角色管理相關操作（建立、查詢、更新、刪除）")
@Slf4j
@RestController
@RequestMapping("/api/upms/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "建立角色", description = "建立角色並可綁定權限代碼")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResult<RoleResp> create(@RequestBody RoleCreateReq req) {
        return ApiResult.success(roleService.create(req));
    }

    @Operation(summary = "查詢角色列表（支援分頁）", description = "回傳所有角色清單")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCH')")
    @GetMapping
    public ApiResult<Page<RoleResp>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
        Page<RoleResp> result = roleService.list(pageable);
        return ApiResult.success(result);
    }

    @Operation(summary = "取得單一角色", description = "依 UUID 查詢指定角色")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<RoleResp>> get(@PathVariable UUID id) {
        RoleResp role = roleService.findById(id);
        return ResponseEntity.ok(ApiResult.success(role));
    }

    @Operation(summary = "更新角色", description = "更新角色名稱與權限綁定")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResult<RoleResp> update(@PathVariable UUID id, @RequestBody RoleUpdateReq req) {
        return ApiResult.success(roleService.update(id, req));
    }

    @Operation(summary = "刪除角色", description = "刪除指定角色（MVP 版本為硬刪除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ApiResult.success(null);
    }
}
