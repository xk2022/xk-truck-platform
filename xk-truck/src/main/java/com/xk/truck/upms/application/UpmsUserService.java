package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.user.*;
import com.xk.truck.upms.domain.model.UpmsUser;
import com.xk.truck.upms.domain.repository.UpmsUserRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ===============================================================
 * Service Class : UpmsUserService
 * Layer         : Application / Domain Service
 * Purpose       : 使用者核心業務流程（建立/查詢/啟用停用/鎖定/重設密碼/指派角色）
 * Notes         :
 * - MVP 先回傳 Entity；未來可切換為 DTO + Mapper
 * - 建議所有寫入操作皆走 @Transactional
 * <p>
 * ✔ 負責：
 * - 建立使用者
 * - 查詢 / 分頁
 * - 啟用 / 停用 / 鎖定
 * - 密碼重設
 * - 角色指派（透過 UserRoleService）
 * <p>
 * ❌ 不負責：
 * - Controller DTO 驗證
 * - Role / Permission 查詢邏輯
 * <p>
 * ===============================================================
 * <p>
 * 設計原則（你這份 Service 的「穩定性」關鍵）
 * 1) Repository 只做資料存取；Service 負責業務流程與一致性（例外、驗證、編碼、關聯維護）
 * 2) 「重複的 findById + orElseThrow」抽成 Guard method（避免 copy-paste + 例外不一致）
 * 3) Username 一律 normalize（避免 Admin/admin 重複 / 空白 / 大小寫）
 * 4) 密碼一律由 Service encode（避免 Controller/DTO 不小心傳入明碼或已 encode 值混亂）
 * 5) 角色指派一律走 UpmsUserRoleService（中介表），避免 UserService 直接操作關聯集合造成耦合擴散
 * <p>
 * 交易邊界（Transaction）
 * - 讀取：@Transactional(readOnly = true)
 * - 寫入：預設 @Transactional（class-level），確保 dirty checking 生效、關聯寫入一致
 * <p>
 * ⚠ 注意：
 * - pageForList() 若你要避免 profile N+1，建議配合 repository 的 EntityGraph/DTO query
 * - roleCodesSnapshot 可能觸發 Lazy（看你 UpmsUserRole/UpmsRole 的 fetch 策略）
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsUserService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_USER_NOT_FOUND = "UPMS_USER_NOT_FOUND";
    private static final String ERR_USER_EXISTS = "UPMS_USER_EXISTS";
    private static final String MSG_USER_NOT_FOUND = "找不到使用者";
    private static final String MSG_USER_EXISTS = "帳號已存在";

    // ===============================================================
    // Repository / Collaborators
    // ===============================================================
    private final UpmsUserRepository userRepository;
    private final UpmsUserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立使用者（含角色）
     * <p>
     * 流程說明：
     * 1) normalize username
     * 2) 檢查 username 唯一
     * 3) 建立 user（密碼必 encode）
     * 4) 儲存 user
     * 5) 指派角色（replaceRoles = 覆蓋式指派）
     */
    public UpmsUserResp create(UpmsUserCreateReq req) {
        // ---- 0) 參數防呆（DTO 驗證通常由 Controller 做，但 Service 仍保留最低限度防護）
        if (req == null) {
            throw new BusinessException("UPMS_USER_REQ_EMPTY", "建立使用者請求不得為空");
        }

        // ---- 1) username normalize（非常關鍵：避免 Admin vs admin 變成兩個帳號）
        final String normalizedUsername = UpmsUser.normalizeUsername(req.getUsername());
        if (!StringUtils.hasText(normalizedUsername)) {
            throw new BusinessException("UPMS_USER_USERNAME_EMPTY", "帳號不能為空");
        }

        log.info("📌 [UpmsUserService] 建立使用者: {}", normalizedUsername);

        // ---- 2) 唯一性檢查（請務必搭配 DB unique constraint，Service 檢查只是提升體驗）
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessException(ERR_USER_EXISTS, MSG_USER_EXISTS);
        }

        // ---- 3) 建立 User（注意：不要把 password 透過 copyProperties 直接塞進去）
        UpmsUser user = new UpmsUser();
        // 你可以繼續用 XkBeanUtils copy，但我建議「白名單欄位」更安全
        // 這裡保留你既有工具，但把敏感欄位改為顯式設定
        XkBeanUtils.copyNonNullProperties(req, user);

        // username 一律以 normalize 後寫入（覆蓋 copy 的結果）
        user.setUsername(normalizedUsername);

        // 密碼：一律 encode
        if (!StringUtils.hasText(req.getPassword())) {
            throw new BusinessException("UPMS_USER_PASSWORD_EMPTY", "密碼不能為空");
        }
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // ---- 4) 儲存 user
        UpmsUser saved = userRepository.save(user);

        // ---- 5) 指派角色（走中介表服務；避免 UserService 直接操作 userRoles 集合）
        if (req.getRoleCodes() != null && !req.getRoleCodes().isEmpty()) {
            userRoleService.replaceRoles(saved.getUuid(), req.getRoleCodes());
        }

        log.info("[UpmsUserService] 使用者建立完成: {} ({})", saved.getUsername(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsUserResp::new);
    }

    // ===============================================================
    // Read - Query / Page
    // ===============================================================

    @Transactional(readOnly = true)
    public UpmsUserResp findById(UUID id) {
        UpmsUser user = loadUserOrThrow(id);
        return XkBeanUtils.copyProperties(user, UpmsUserResp::new);
    }

    @Transactional(readOnly = true)
    public UpmsUserResp findByUsername(String username) {
        UpmsUser user = loadUserByUsernameOrThrow(username);
        return XkBeanUtils.copyProperties(user, UpmsUserResp::new);
    }

    /**
     * 分頁查詢（後台列表）
     * <p>
     * ⚠ 注意：此方法目前使用 findAll(spec, pageable) + map
     * - 如果你 profile 是 LAZY，這裡會有 N+1 風險
     * - 你可改用：
     * A) Repository: findAllWithProfile(spec, pageable) + @EntityGraph
     * B) DTO Query: 直接 select new UpmsUserListResp(...)
     * <p>
     * 你目前先做 MVP，我保留現況，但把 mapping 區塊整理得更一致。
     */
    @Transactional(readOnly = true)
    public Page<UpmsUserListResp> pageForList(UpmsUserQuery query, Pageable pageable) {
        Specification<UpmsUser> spec = buildUserSpec(query);

        return userRepository.findAll(spec, pageable)
//                .map(u -> XkBeanUtils.copyProperties(u, UserResp::new));
                .map(user -> {
                    UpmsUserListResp dto = new UpmsUserListResp();
                    dto.setId(user.getUuid());
                    dto.setUsername(user.getUsername());

                    if (user.getProfile() != null) {
                        dto.setName(user.getProfile().getName());
                        dto.setEmail(user.getProfile().getEmail());
                        dto.setAvatarUrl(user.getProfile().getAvatarUrl());
                    } else {
                        dto.setName(user.getUsername());
                    }

                    dto.setEnabled(user.getEnabled());
                    dto.setLocked(user.getLocked());
                    dto.setLastLoginAt(user.getLastLoginAt());
                    dto.setJoinedAt(user.getCreatedTime());

                    // Role codes snapshot：可能觸發 lazy（你已在 UpmsUser 做 snapshot，OK）
                    dto.setRoleCodes(user.getRoleCodesSnapshot());

                    dto.setTwoStepsEnabled(false); // 預留（未來接 MFA 設定）
                    return dto;
                });
    }

    /**
     * 動態組合 User 查詢條件（Specification）
     * <p>
     * 原則：
     * - query==null → 全查（cb.conjunction）
     * - username → like（lower + %keyword%）
     * - enabled/locked → equal
     * - roleCode → join userRoles.role，並 cq.distinct(true) 避免重複 row
     * <p>
     * ⚠ roleCode 查詢地雷：
     * - join 會讓 count query 變複雜
     * - 必須 distinct
     * - 若資料量大，可能改用子查詢或先查 userId 再查 users（視效能需求）
     */
    private Specification<UpmsUser> buildUserSpec(UpmsUserQuery query) {
        return (root, cq, cb) -> {
            if (query == null) return cb.conjunction();

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query.getUsername())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("username")),
                                "%" + query.getUsername().toLowerCase() + "%"
                        )
                );
            }

            if (query.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), query.getEnabled()));
            }

            if (query.getLocked() != null) {
                predicates.add(cb.equal(root.get("locked"), query.getLocked()));
            }

            if (StringUtils.hasText(query.getRoleCode())) {
                var userRoleJoin = root.join("userRoles", JoinType.LEFT);
                var roleJoin = userRoleJoin.join("role", JoinType.LEFT);

                predicates.add(cb.equal(roleJoin.get("code"), query.getRoleCode()));
                cq.distinct(true);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // ===============================================================
    // Update - Basic / Status
    // ===============================================================

    /**
     * 更新基本資料（不含角色、不含密碼）
     */
    public UpmsUserResp updateBasic(UUID id, UpmsUserUpdateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_USER_UPDATE_REQ_EMPTY", "更新資料請求不得為空");
        }

        // 1) 更新基本
        UpmsUser saved = updateBasicInternal(id, req);

        log.info("✏️ [UpmsUserService] 使用者基本資料更新: {} ({})", saved.getUsername(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsUserResp::new);
    }

    /**
     * 覆蓋式更新（基本 + 角色）
     * <p>
     * 規則：
     * - req.roleCodes != null → 覆蓋式 replace
     * - req.roleCodes == null → 不動角色（保持原狀）
     */
    public UpmsUserResp updateAll(UUID id, UpmsUserUpdateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_USER_UPDATE_REQ_EMPTY", "更新資料請求不得為空");
        }

        // 1) 更新基本
        UpmsUser saved = updateBasicInternal(id, req);

        // 2) 更新角色（若有帶 roleCodes）
        if (req.getRoleCodes() != null) {
            // clearRoles + assignRole
            userRoleService.replaceRoles(id, req.getRoleCodes());
        }

        log.info("✏️ [UpmsUserService] 更新使用者成功: {} ({})", saved.getUsername(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsUserResp::new);
    }

    /**
     * 更新基本欄位共用流程（內部）
     * <p>
     * 排雷點：
     * - 永遠使用 Guard method loadUserOrThrow()（避免 exception code 不一致）
     * - 不要更新 password（密碼走 resetPassword/changePassword）
     * - 若允許更新 username，務必 normalize + unique check（這裡我預設「不允許」或「要非常謹慎」）
     */
    private UpmsUser updateBasicInternal(UUID id, UpmsUserUpdateReq req) {
        UpmsUser user = loadUserOrThrow(id);

        // ⚠ 若你 UpmsUserUpdateReq 內包含 username，建議在這裡決定是否允許改帳號
        // 你目前 DTO 沒貼出來，我先用「如果有提供 username 就 normalize 並檢查唯一」的安全版本
        if (StringUtils.hasText(req.getUsername())) {
            String newUsername = UpmsUser.normalizeUsername(req.getUsername());
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new BusinessException(ERR_USER_EXISTS, MSG_USER_EXISTS);
                }
                user.changeUsername(newUsername);
            }
        }

        // 其餘欄位：copy non-null
        // 注意：copyNonNullProperties 可能把 password/locked/loginFailCount 等也寫進來（看 req 欄位）
        // 因此你要確保 UpmsUserUpdateReq 不含敏感欄位，或在 copy 後再覆蓋保護
        XkBeanUtils.copyNonNullProperties(req, user);

        // 防護：避免 req 不小心帶入 password 破壞安全流程（若你的 DTO 真的沒有 password，可留著當保險）
        // user.setPassword(user.getPassword());

        return userRepository.save(user);
    }

    // ===============================================================
    // Security operations
    // ===============================================================

    /**
     * 啟用 / 停用
     * <p>
     * 為什麼這裡可以不呼叫 save？
     * - 因為 class-level 已 @Transactional
     * - loadUserOrThrow() 取得的是 managed entity
     * - setEnabled() 後，Hibernate dirty checking 會在 transaction commit 時自動 flush
     * <p>
     * 如果你想「立即落庫」或「避免 session 依賴」，可以改用 repository updateEnabled(...) bulk update。
     */
    public void updateEnabled(UUID id, boolean enabled) {
        UpmsUser user = loadUserOrThrow(id);
        user.setEnabled(enabled);

        log.info("🔄 [UpmsUserService] 使用者狀態更新: {} -> {}", user.getUsername(), enabled ? "啟用" : "停用");
    }

    /**
     * 重設密碼（由 Service encode）
     * <p>
     * ✅ 你 UpmsUser 已提供 domain method changePassword(encoded, now)
     * - 這很好：避免外部直接 setPassword
     */
    public void resetPassword(UUID id, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("UPMS_USER_PASSWORD_EMPTY", "新密碼不能為空");
        }

        UpmsUser user = loadUserOrThrow(id);
        user.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());

        log.info("🔑 [UpmsUserService] 使用者密碼已重設: {} ({})", user.getUsername(), user.getUuid());
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除使用者
     * <p>
     * 建議流程：
     * 1) 確認存在（loadUserOrThrow）
     * 2) 清關聯（userRoles / profile… 視你的 cascade 設計）
     * 3) deleteById
     * <p>
     * ⚠ 若 DB 有 FK 約束：
     * - 先刪 user_roles 再刪 user（你已做 userRoleService.clearRoles(id)）
     * - profile 若 cascade=ALL + orphanRemoval，通常跟著刪；但你要確認 profile mapping 設計
     */
    public void delete(UUID id) {
        // guard
        this.loadUserOrThrow(id);

        // 先清角色關聯（避免 FK constraint）
        userRoleService.clearRoles(id);

        userRepository.deleteById(id);
        log.info("🗑️ [UpmsUserService] 使用者已刪除: {}", id);
    }

    // ===============================================================
    // Validation / Exists
    // ===============================================================

    public boolean existsByUsername(String username) {
        String normalized = UpmsUser.normalizeUsername(username);
        if (!StringUtils.hasText(normalized)) return false;
        return userRepository.existsByUsername(normalized);
    }

    // ===============================================================
    // Internal Guard / Loader
    // ===============================================================

    /**
     * Guard method：載入使用者，找不到就丟一致的 BusinessException
     * - 統一錯誤代碼 / 訊息
     * - 統一日後替換 query（例如改成 findWithProfileByUuid）
     */
    private UpmsUser loadUserOrThrow(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_USER_ID_EMPTY", "使用者 ID 不得為空");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));
    }

    /**
     * Guard method：依 username 載入使用者
     * - 一樣 normalize
     */
    private UpmsUser loadUserByUsernameOrThrow(String username) {
        String normalized = UpmsUser.normalizeUsername(username);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("UPMS_USER_USERNAME_EMPTY", "帳號不能為空");
        }
        return userRepository.findByUsername(normalized)
                .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));
    }
}
