package com.xk.truck.upms.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.upms.application.UpmsUserProfileService;
import com.xk.truck.upms.controller.api.dto.profile.UserProfileReq;
import com.xk.truck.upms.controller.api.dto.profile.UserProfileResp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ===============================================================
 * Controller Class : UpmsUserProfileController
 * Layer            : Controller (API)
 * Purpose          : 使用者 Profile 操作（查詢、更新）
 *
 * Controller 設計原則（對齊 UpmsUserController）
 * 1) Controller 不做業務邏輯：不操作 entity、不做 guard、不做 copy
 * 2) 例外與規則統一由 Service 處理（BusinessException）
 * 3) DTO 驗證交給 @Valid / @Validated
 * ===============================================================
 */
@Tag(name = "UPMS - User Profile API", description = "使用者 Profile 操作（查詢、更新）")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upms/users/{userId}/profile")
public class UpmsUserProfileController {

    private final UpmsUserProfileService userProfileService;

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 取得使用者完整 Profile（含 user 基本資訊 + profile）
     * GET /api/upms/users/{userId}/profile
     */
    @Operation(summary = "取得使用者 Profile（整合輸出）")
    @GetMapping
    public ApiResult<UserProfileResp> get(@PathVariable("userId") UUID userId) {
        return ApiResult.success(userProfileService.buildProfileResp(userId));
    }

    /**
     * 取得 Profile 純資料（僅 profile 欄位）
     * GET /api/upms/users/{userId}/profile/basic
     *
     * 可選：若你不需要「純 profile」，可以刪掉這支。
     */
    @Operation(summary = "取得使用者 Profile（僅 Profile 欄位）")
    @GetMapping("/basic")
    public ApiResult<UserProfileResp> getBasic(@PathVariable("userId") UUID userId) {
        return ApiResult.success(userProfileService.getProfile(userId));
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新使用者 Profile（部分欄位更新）
     * PATCH /api/upms/users/{userId}/profile
     *
     * - 對齊 UpmsUserController：更新類操作建議用 PATCH
     * - Service 使用 copyNonNullProperties，因此 PATCH 語意非常吻合
     */
    @Operation(summary = "更新使用者 Profile（部分欄位）")
    @PatchMapping
    public ApiResult<UserProfileResp> update(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserProfileReq req
    ) {
        return ApiResult.success(userProfileService.updateProfile(userId, req));
    }

    /**
     * 若你仍想保留 PUT（覆蓋式更新）路由，這裡可保留成 alias
     * - 目前 Service 是「non-null 才覆蓋」，所以 PUT/PATCH 實際效果相同
     * - 建議保留 PATCH 作為主線，PUT 視前端需求決定要不要留
     */
    @Operation(summary = "更新使用者 Profile（相容 PUT）", description = "目前行為與 PATCH 相同：僅更新有提供的欄位")
    @PutMapping
    public ApiResult<UserProfileResp> updateCompatPut(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserProfileReq req
    ) {
        return ApiResult.success(userProfileService.updateProfile(userId, req));
    }
}
