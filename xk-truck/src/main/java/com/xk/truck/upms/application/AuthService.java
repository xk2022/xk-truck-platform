package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.base.security.JwtService;
import com.xk.base.security.JwtUtils;
import com.xk.truck.upms.controller.api.dto.auth.LoginRequest;
import com.xk.truck.upms.controller.api.dto.auth.LoginResponse;
import com.xk.truck.upms.controller.api.dto.auth.MeResponse;
import com.xk.truck.upms.controller.api.dto.auth.RefreshTokenResponse;
import com.xk.truck.upms.domain.model.*;

import com.xk.truck.upms.domain.repository.UpmsRolePermissionRepository;
import com.xk.truck.upms.domain.repository.UpmsUserRepository;

import com.xk.truck.upms.domain.repository.UpmsUserRoleRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ===============================================================
 * Service Class : AuthService
 * Layer         : Application / Domain Service
 * Purpose       : 認證相關流程（Login / Me / RefreshToken）
 * <p>
 * 設計原則（對齊你 UpmsUserService 風格）
 * 1) Repository 只做資料存取；Service 管流程、例外一致性、狀態更新
 * 2) Guard method 集中：避免 everywhere findByUsername + throw
 * 3) 登入失敗/成功狀態更新：優先用 repository bulk update（少一次 save、避免 entity 髒檢查副作用）
 * 4) 權限計算：一次拿 userRoles，再拿 rolePermissions（避免多次查 DB）
 * <p>
 * 排雷重點
 * - username 必須 normalize（避免 Admin/admin）
 * - 密碼錯誤要更新 failCount（可搭配鎖定策略）
 * - SecurityContext 可能是 anonymousUser
 * - refresh token：要處理 roles claim 不存在 / 格式不一
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    // ===============================================================
    // Error Code / Message（集中管理）
    // ===============================================================
    private static final String ERR_AUTH_BAD_CREDENTIALS = "AUTH_BAD_CREDENTIALS";
    private static final String ERR_AUTH_DISABLED = "AUTH_ACCOUNT_DISABLED";
    private static final String ERR_AUTH_LOCKED = "AUTH_ACCOUNT_LOCKED";
    private static final String ERR_AUTH_UNAUTHORIZED = "AUTH_UNAUTHORIZED";
    private static final String ERR_AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
    private static final String ERR_USER_NOT_FOUND = "UPMS_USER_NOT_FOUND";

    private static final String MSG_BAD_CREDENTIALS = "帳號或密碼錯誤";
    private static final String MSG_ACCOUNT_DISABLED = "帳號已停用";
    private static final String MSG_ACCOUNT_LOCKED = "帳號已被鎖定";
    private static final String MSG_UNAUTHORIZED = "尚未登入或 Token 無效";
    private static final String MSG_TOKEN_INVALID = "Token 無效或已過期";
    private static final String MSG_USER_NOT_FOUND = "找不到目前登入者";

    // ===============================================================
    // Dependencies
    // ===============================================================
    private final UpmsUserRepository userRepository;
    private final UpmsUserRoleRepository userRoleRepository;
    private final UpmsRolePermissionRepository rolePermissionRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 若希望 TTL 走全域設定，這裡可改成 null 讓 JwtService 使用預設配置
     * 目前保留你原先 2 小時。
     */
    private static final Duration LOGIN_TTL = Duration.ofHours(2);

    // ===============================================================
    // Login
    // ===============================================================

    /**
     * 登入流程：
     * 1) normalize username
     * 2) 以 username 找 user
     * 3) 檢查 enabled / locked
     * 4) 驗證密碼
     * - 失敗：failCount + 1（可搭配鎖定策略）
     * - 成功：failCount=0, lastLoginAt=now
     * 5) 取得 roleCodes / permissionCodes
     * 6) 產生 JWT
     * 7) 組合 LoginResponse（含 me）
     */
    public LoginResponse login(LoginRequest request) {

        // ---- 0) 最低限度防呆（Controller validation 仍建議要做）
        if (request == null) {
            throw new BusinessException("AUTH_REQ_EMPTY", "登入請求不得為空");
        }

        final String normalizedUsername = UpmsUser.normalizeUsername(request.getUsername());
        if (!StringUtils.hasText(normalizedUsername) || !StringUtils.hasText(request.getPassword())) {
            // 這裡統一回「帳密錯誤」避免帳號探測
            throw new BusinessException(ERR_AUTH_BAD_CREDENTIALS, MSG_BAD_CREDENTIALS);
        }

        log.info("🔐 [AuthService] login: {}", normalizedUsername);

        // ---- 1) guard: 找 user（找不到也回帳密錯誤避免探測）
        UpmsUser user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new BusinessException(ERR_AUTH_BAD_CREDENTIALS, MSG_BAD_CREDENTIALS));

        // ---- 2) account state check
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException(ERR_AUTH_DISABLED, MSG_ACCOUNT_DISABLED);
        }
        if (Boolean.TRUE.equals(user.getLocked())) {
            throw new BusinessException(ERR_AUTH_LOCKED, MSG_ACCOUNT_LOCKED);
        }

        // ---- 3) verify password
        boolean ok = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!ok) {
            // 登入失敗：failCount + 1
            int currentFail = Optional.ofNullable(user.getLoginFailCount()).orElse(0);
            int nextFail = currentFail + 1;

            // 建議用 bulk update（避免 save 整個 entity / 避免不必要的欄位被覆蓋）
            userRepository.updateLockState(
                    user.getUuid(),
                    user.getLocked(),      // 先不做鎖定策略，保留原值
                    user.getLockedAt(),    // 保留
                    nextFail
            );

            // ❗如果你要做到「連續 N 次鎖定」：
            // - 可在這裡判斷 nextFail >= N，然後 updateLockState(... locked=true, lockedAt=now, failCount=nextFail)
            // - 但鎖定策略是否要寫在 AuthService 或 UpmsUserService，看你架構偏好

            throw new BusinessException(ERR_AUTH_BAD_CREDENTIALS, MSG_BAD_CREDENTIALS);
        }

        // ---- 4) login success: reset failCount & update lastLoginAt
        // 你已經有 updateLastLoginAt / updateLockState，這裡用兩個 bulk update，較乾淨
        userRepository.updateLockState(
                user.getUuid(),
                false,                  // 登入成功順便解鎖（你要不要解鎖可自行決定；若不想解鎖就用 user.getLocked()）
                null,
                0
        );
        userRepository.updateLastLoginAt(user.getUuid(), LocalDateTime.now());

        // ---- 5) roles / permissions
        AuthSnapshot snapshot = loadAuthSnapshot(user.getUuid());
        Set<String> roleCodes = snapshot.roleCodes();
        Set<String> permissionCodes = snapshot.permissionCodes();

        // ---- 6) generate JWT
        String token = jwtService.generate(
                user.getUsername(),
                roleCodes.toArray(String[]::new),
                null,
                LOGIN_TTL
        );

        // ---- 7) build me + response
        // 這裡的 user entity 可能是舊狀態（因為我們用 bulk update），
        // 但回傳 me 主要是 profile/roles/permissions，不依賴 lastLoginAt 即可。
        // 若你希望回傳 lastLoginAt 最新值，建議重新查一次或改用 managed entity update。
        MeResponse me = buildMeResponse(user, roleCodes, permissionCodes);

        return LoginResponse.builder()
                .accessToken(token)
                .me(me)
                .build();
    }

    // ===============================================================
    // Me
    // ===============================================================

    /**
     * 取得目前登入者資訊（從 SecurityContext 拿 username）
     * - 這裡通常是 API: GET /api/auth/me
     */
    @Transactional(readOnly = true)
    public MeResponse me() {
        String username = currentUsernameOrThrow();

        UpmsUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));

        AuthSnapshot snapshot = loadAuthSnapshot(user.getUuid());
        return buildMeResponse(user, snapshot.roleCodes(), snapshot.permissionCodes());
    }

    // ===============================================================
    // Refresh Token
    // ===============================================================

    /**
     * Refresh token：
     * - 讀取 Bearer token
     * - parse 舊 token
     * - 取 subject/roles
     * - generate 新 token
     * <p>
     * ⚠ 注意：
     * - 你現在的設計是「同一顆 access token refresh」，
     * 若未來要 refresh token + access token 分離，要另做 RefreshToken entity/blacklist。
     */
    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(String bearerToken) {
        String oldToken = extractBearerTokenOrThrow(bearerToken);

        try {
            var claims = jwtService.parse(oldToken).getBody();

            String username = claims.getSubject();
            if (!StringUtils.hasText(username)) {
                throw new BusinessException(ERR_AUTH_TOKEN_INVALID, MSG_TOKEN_INVALID);
            }

            String[] roles = JwtUtils.normalizeRoles(claims.get("roles"));
            // roles 可能為 null/空，normalizeRoles 應能處理；若不能，這裡要保護
            if (roles == null) roles = new String[0];

            String newToken = jwtService.generate(username, roles, null, LOGIN_TTL);

            RefreshTokenResponse resp = new RefreshTokenResponse();
            resp.setToken(newToken);
            resp.setType("Bearer");
            return resp;

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(ERR_AUTH_TOKEN_INVALID, MSG_TOKEN_INVALID);
        }
    }

    // ===============================================================
    // Snapshot loaders（集中查角色/權限，避免重複 DB call）
    // ===============================================================

    /**
     * 一次載入「角色 + 權限」快照
     * <p>
     * ✅ 好處：
     * - login() / me() 都能共用
     * - DB 查詢次數固定且可控
     * <p>
     * 預設查詢策略：
     * 1) userRoleRepository.findByUserUuid(userUuid)
     * 2) 從 userRoles 抽出 roleUuid
     * 3) rolePermissionRepository.findAllByRoleUuidIn(roleUuids)
     */
    @Transactional(readOnly = true)
    protected AuthSnapshot loadAuthSnapshot(UUID userUuid) {
        List<UpmsUserRole> userRoles = userRoleRepository.findByUserUuid(userUuid);

        // roles
        LinkedHashSet<String> roleCodes = userRoles.stream()
                .map(UpmsUserRole::getRole)
                .filter(Objects::nonNull)
                .map(UpmsRole::getCode) // ✅ 修正：不要用 UpmsSeedProps.Role
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // roleUuid set（避免 entity equals/hashCode 不穩定，用 uuid 最穩）
        Set<UUID> roleUuids = userRoles.stream()
                .map(UpmsUserRole::getRole)
                .filter(Objects::nonNull)
                .map(UpmsRole::getUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (roleUuids.isEmpty()) {
            return new AuthSnapshot(roleCodes, Collections.emptySet());
        }

        List<UpmsRolePermission> rolePermissions = rolePermissionRepository.findAllByRoleUuidIn(roleUuids);

        LinkedHashSet<String> permissionCodes = rolePermissions.stream()
                .map(UpmsRolePermission::getPermission)
                .filter(Objects::nonNull)
                .map(UpmsPermission::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new AuthSnapshot(roleCodes, permissionCodes);
    }

    /**
     * AuthSnapshot：避免方法回傳兩個 Set 用 Pair/Map 造成可讀性下降
     */
    protected record AuthSnapshot(Set<String> roleCodes, Set<String> permissionCodes) {
    }

    // ===============================================================
    // Response builder
    // ===============================================================

    /**
     * buildMeResponse：
     * - 只做 DTO 組裝
     * - 不做查詢 / 不做業務邏輯
     */
    protected MeResponse buildMeResponse(
            UpmsUser user,
            Set<String> roleCodes,
            Set<String> permissionCodes
    ) {
        UpmsUserProfile profile = user.getProfile();

        return MeResponse.builder()
                .userId(user.getUuid())
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .loginFailCount(user.getLoginFailCount())
                .lastLoginAt(user.getLastLoginAt())
                .name(profile != null ? profile.getName() : null)
                .nickName(profile != null ? profile.getNickName() : null)
                .email(profile != null ? profile.getEmail() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .roleCodes(roleCodes)
                .permissionCodes(permissionCodes)
                .build();
    }

    // ===============================================================
    // Guard / Helpers
    // ===============================================================

    /**
     * 從 SecurityContext 取目前 username（含 anonymous 排雷）
     */
    protected String currentUsernameOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            throw new BusinessException(ERR_AUTH_UNAUTHORIZED, MSG_UNAUTHORIZED);
        }

        // Spring Security 預設 getName() 就是 username
        String username = authentication.getName();
        String normalized = UpmsUser.normalizeUsername(username);

        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ERR_AUTH_UNAUTHORIZED, MSG_UNAUTHORIZED);
        }
        return normalized;
    }

    /**
     * Bearer token extractor（集中錯誤處理）
     */
    protected String extractBearerTokenOrThrow(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(ERR_AUTH_TOKEN_INVALID, "缺少 Bearer Token");
        }
        return bearerToken.substring(7);
    }
}
