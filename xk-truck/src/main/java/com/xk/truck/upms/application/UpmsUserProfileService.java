package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.profile.UserProfileReq;
import com.xk.truck.upms.controller.api.dto.profile.UserProfileResp;
import com.xk.truck.upms.domain.model.UpmsUser;
import com.xk.truck.upms.domain.model.UpmsUserProfile;
import com.xk.truck.upms.domain.repository.UpmsUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ===============================================================
 * Service Class : UpmsUserProfileService
 * Layer         : Application / Domain Service
 * Purpose       : 使用者 Profile 業務流程（查詢/更新/組合輸出）
 * <p>
 * 設計原則（對齊 UpmsUserService）
 * 1) Repository 只做資料存取；Service 負責一致性/例外/關聯維護
 * 2) 重複的 findById + orElseThrow → Guard method
 * 3) 讀取 readOnly；寫入 default transactional
 * 4) Profile 若不存在：update 時自動建立（MVP 友善）
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsUserProfileService {

    // ===============================================================
    // Error Code / Message（集中管理）
    // ===============================================================
    private static final String ERR_USER_NOT_FOUND = "UPMS_USER_NOT_FOUND";
    private static final String MSG_USER_NOT_FOUND = "找不到使用者";

    private static final String ERR_PROFILE_REQ_EMPTY = "UPMS_USER_PROFILE_REQ_EMPTY";
    private static final String MSG_PROFILE_REQ_EMPTY = "更新 Profile 請求不得為空";

    // ===============================================================
    // Repository
    // ===============================================================
    private final UpmsUserRepository userRepository;

    // ===============================================================
    // Read
    // ===============================================================

    /**
     * 取得 Profile（不存在就回空 DTO）
     * - 若你希望「沒 profile 就拋錯」，把空回傳改成 exception 即可
     */
    @Transactional(readOnly = true)
    public UserProfileResp getProfile(UUID userId) {
        UpmsUser user = loadUserOrThrow(userId);

        UpmsUserProfile profile = user.getProfile();
        if (profile == null) {
            return new UserProfileResp();
        }
        return XkBeanUtils.copyProperties(profile, UserProfileResp::new);
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新 Profile（不存在就建立）
     * - copyNonNullProperties：只覆蓋有提供的欄位
     * - 雙向關聯：profile.setUser(user)
     */
    public UserProfileResp updateProfile(UUID userId, UserProfileReq req) {
        if (req == null) {
            throw new BusinessException(ERR_PROFILE_REQ_EMPTY, MSG_PROFILE_REQ_EMPTY);
        }

        UpmsUser user = loadUserOrThrow(userId);

        UpmsUserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UpmsUserProfile();
            // 維護雙向（若你是單向，保留 user.setProfile(profile) 即可）
            profile.setUser(user);
            user.setProfile(profile);
        }

        XkBeanUtils.copyNonNullProperties(req, profile);

        // 走 dirty checking 或 save 都可；為一致性保留 save（也能處理 cascade 不明確時的落庫）
        UpmsUser saved = userRepository.save(user);

        log.info(
                "✏️ [UpmsUserProfileService] 更新使用者 Profile: {} ({})",
                saved.getUsername(), saved.getUuid()
        );

        return XkBeanUtils.copyProperties(saved.getProfile(), UserProfileResp::new);
    }

    // ===============================================================
    // Composite output（保留你的 buildProfileResp，但對齊 UpmsUser）
    // ===============================================================

    /**
     * 組合完整 Profile Response（含 user 基本資訊 + profile）
     * <p>
     * ⚠ 注意：
     * - 你原本還想組 roles/permissions/loginHistory
     * - 但 UpmsUserRepository 目前只保證 profile EntityGraph（findWithProfileByUuid）
     * - 若要避免 roles lazy/N+1：建議另做 repository entityGraph 或由專門 service 查（UserRoleService / RolePermissionService）
     */
    @Transactional(readOnly = true)
    public UserProfileResp buildProfileResp(UUID userId) {
        UpmsUser user = loadUserOrThrow(userId);

        UserProfileResp resp = new UserProfileResp();
        resp.setId(user.getUuid());
        resp.setUsername(user.getUsername());
        resp.setEnabled(user.getEnabled());
        resp.setLocked(user.getLocked());

        if (user.getProfile() != null) {
            var p = user.getProfile();
            UserProfileResp.ProfileResp pp = new UserProfileResp.ProfileResp();
            pp.setName(p.getName());
            pp.setNickName(p.getNickName());
            pp.setEmail(p.getEmail());
            pp.setPhone(p.getPhone());
            pp.setAvatarUrl(p.getAvatarUrl());
            resp.setProfile(pp);
        }

        // Roles / Permissions / Login history：
        // 建議由對應 Service 負責（避免在 ProfileService 內碰太多關聯造成耦合擴散）

        return resp;
    }

    // ===============================================================
    // Internal Guard / Loader（對齊 UpmsUserService）
    // ===============================================================

    private UpmsUser loadUserOrThrow(UUID userId) {
        if (userId == null) {
            throw new BusinessException("UPMS_USER_ID_EMPTY", "使用者 ID 不得為空");
        }

        // 若你想穩定避免 profile lazy：可改成 findWithProfileByUuid(userId)
        // return userRepository.findWithProfileByUuid(userId)
        //        .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));

        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));
    }
}
