package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.truck.upms.domain.model.UpmsRole;
import com.xk.truck.upms.domain.model.UpmsUser;
import com.xk.truck.upms.domain.model.UpmsUserRole;
import com.xk.truck.upms.domain.repository.UpmsRoleRepository;
import com.xk.truck.upms.domain.repository.UpmsUserRepository;
import com.xk.truck.upms.domain.repository.UpmsUserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ===============================================================
 * Service Class : UpmsUserRoleService
 * Layer         : Application / Domain Service
 * Purpose       : 使用者 ↔ 角色（UpmsUserRole）關聯的核心業務流程
 * Notes         :
 * - UpmsUserService 不直接操作 user.getUserRoles()，一律由此 Service 統一維護關聯一致性
 * - 避免 Set/orphanRemoval/equals-hc 的各種坑：以「Repository query + 中介表 CRUD」為主
 * - replaceRoles() 採「覆蓋式」策略：傳什麼就變成什麼（常用於後台編輯使用者角色）
 * <p>
 * ✔ 負責：
 * - assignRole / removeRole
 * - replaceRoles（覆蓋式指派）
 * - clearRoles（清空）
 * <p>
 * ❌ 不負責：
 * - Role / Permission 的業務判斷（例如是否可指派、是否需要某些 permission），那是更上層策略
 * <p>
 * ===============================================================
 * <p>
 * 設計原則（穩定性關鍵）
 * 1) 所有 roleCode 先 normalize（trim + upper），避免 "sys_admin" / " SYS_ADMIN " 問題
 * 2) 所有寫入操作都要 Transaction，避免 delete + insert 半套狀態
 * 3) Repository 僅資料存取；Service 統一例外、驗證、資料一致性
 * 4) 不走「直接操作 entity collection」來同步關聯（避免 orphanRemoval、equals/hashCode、lazy 觸發）
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsUserRoleService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_USER_NOT_FOUND = "UPMS_USER_NOT_FOUND";
    private static final String ERR_ROLE_NOT_FOUND = "UPMS_ROLE_NOT_FOUND";
    private static final String ERR_ROLE_CODE_EMPTY = "UPMS_ROLE_CODE_EMPTY";
    private static final String ERR_USER_ID_EMPTY = "UPMS_USER_ID_EMPTY";

    private static final String MSG_USER_NOT_FOUND = "找不到使用者";
    private static final String MSG_ROLE_NOT_FOUND = "找不到角色";
    private static final String MSG_ROLE_CODE_EMPTY = "角色代碼不能為空";
    private static final String MSG_USER_ID_EMPTY = "使用者 ID 不得為空";

    // ===============================================================
    // Collaborators
    // ===============================================================
    private final UpmsUserRoleRepository userRoleRepository;
    private final UpmsUserRepository userRepository;
    private final UpmsRoleRepository roleRepository;

    // ===============================================================
    // Public APIs
    // ===============================================================

    /**
     * 指派單一角色給使用者（增量）
     * <p>
     * 特性：
     * - 若已存在關聯 → 視為成功（idempotent）
     * - 若 role 不存在 → 拋 BusinessException
     */
    public void assignRole(UUID userId, String roleCode) {
        UpmsUser user = loadUserOrThrow(userId);

        final String normalizedRoleCode = normalizeRoleCode(roleCode);
        UpmsRole role = loadRoleByCodeOrThrow(normalizedRoleCode);

        // 已存在關聯就不重複新增（避免 unique constraint / 重複 row）
        boolean exists = userRoleRepository.existsByUserUuidAndRoleUuid(user.getUuid(), role.getUuid());
        if (exists) {
            log.info(
                    "[UpmsUserRoleService] assignRole skipped (already exists): user={}, role={}",
                    user.getUsername(), normalizedRoleCode
            );
            return;
        }

        // 若你的 UpmsUserRole 有 effectiveFrom/effectiveTo，可在這裡設定預設值（不強制）
        // link.setEffectiveFrom(LocalDateTime.now());
        try {
            UpmsUserRole link = new UpmsUserRole(user, role);
            userRoleRepository.save(link);
        } catch (DataIntegrityViolationException e) {
            // 同一組 (user_uuid, role_uuid) 已存在 → 當作成功
            log.info("ℹ️ assignRole ignored duplicate: user={}, role={}", user.getUuid(), role.getUuid());
        }

        log.info(
                "[UpmsUserRoleService] assignRole ok: user={}({}), role={}({})",
                user.getUsername(), user.getUuid(), role.getCode(), role.getUuid()
        );
    }

    /**
     * 移除單一角色（減量）
     * <p>
     * 特性：
     * - 若關聯不存在 → 視為成功（idempotent）
     */
    public void removeRole(UUID userId, String roleCode) {
        UpmsUser user = loadUserOrThrow(userId);

        final String normalizedRoleCode = normalizeRoleCode(roleCode);
        UpmsRole role = loadRoleByCodeOrThrow(normalizedRoleCode);

        int affected = userRoleRepository.deleteByUserUuidAndRoleUuid(user.getUuid(), role.getUuid());
        log.info(
                "🧹 [UpmsUserRoleService] removeRole: user={}({}), role={}({}), affected={}",
                user.getUsername(), user.getUuid(), role.getCode(), role.getUuid(), affected
        );
    }

    /**
     * 清空使用者所有角色
     * <p>
     * 用途：
     * - 刪除使用者前先清關聯（避免 FK constraint）
     * - 或後台「移除全部角色」的情境
     */
    public void clearRoles(UUID userId) {
        UpmsUser user = loadUserOrThrow(userId);

        int affected = userRoleRepository.deleteByUserUuid(user.getUuid());
        log.info(
                "🧹 [UpmsUserRoleService] clearRoles: user={}({}), affected={}",
                user.getUsername(), user.getUuid(), affected
        );
    }

    /**
     * 覆蓋式指派角色（replace）
     * <p>
     * 規則（非常重要，避免 UI 行為不一致）：
     * - roleCodes == null → 當成「不操作」（由呼叫端決定）
     * - roleCodes is empty → 清空所有角色
     * - roleCodes 有值 → 最終關聯 = 這批 roleCodes（以傳入順序去重）
     * <p>
     * 實作策略：
     * - 先 normalize + 去重（LinkedHashSet 保序）
     * - 一次查出所有 roles（findAllByCodeIn）
     * - 若有不存在的 roleCode → 丟錯（避免靜默漏指派）
     * - 以「差集」方式：刪除不在目標集合的關聯、補上缺少的關聯
     * <p>
     * 這樣做的好處：
     * - 不必「先全刪再全建」(但你也可以全刪全建，這版是更穩的差集策略)
     * - 避免中途失敗造成角色全部消失（transaction 可保護，但差集更利於 audit）
     */
    public void replaceRoles(UUID userId, Collection<String> roleCodes) {
        UpmsUser user = loadUserOrThrow(userId);

        // 1) 呼叫端規則：null = 不處理（你 UpmsUserService 是用「!= null 才 replace」，所以這裡照做）
        if (roleCodes == null) {
            log.info(
                    "ℹ️ [UpmsUserRoleService] replaceRoles skipped (roleCodes is null): user={}({})",
                    user.getUsername(), user.getUuid()
            );
            return;
        }

        // 2) empty = 清空
        LinkedHashSet<String> normalizedTargetCodes = normalizeRoleCodes(roleCodes);
        if (normalizedTargetCodes.isEmpty()) {
            clearRoles(user.getUuid());
            return;
        }

        // 3) 一次查出所有 role（避免 N+1）
        List<UpmsRole> roles = roleRepository.findAllByCodeIn(normalizedTargetCodes);

        // 4) 檢查是否有不存在的 roleCode（非常重要：避免 UI 傳錯卻 silent）
        Set<String> foundCodes = new HashSet<>();
        for (UpmsRole r : roles) {
            if (r != null && r.getCode() != null) foundCodes.add(r.getCode());
        }
        List<String> missing = new ArrayList<>();
        for (String code : normalizedTargetCodes) {
            if (!foundCodes.contains(code)) missing.add(code);
        }
        if (!missing.isEmpty()) {
            throw new BusinessException(
                    ERR_ROLE_NOT_FOUND,
                    MSG_ROLE_NOT_FOUND + "（不存在 roleCode: " + String.join(", ", missing) + "）"
            );
        }

        // 5) 取得現有關聯（只取 role_uuid 以做差集，不要把整個 graph 拉出來）
        Set<UUID> currentRoleUuids = userRoleRepository.findRoleUuidsByUserUuid(user.getUuid());
        Set<UUID> targetRoleUuids = new LinkedHashSet<>();
        for (UpmsRole r : roles) targetRoleUuids.add(r.getUuid());

        // 6) 刪除：存在於 current，但不在 target
        Set<UUID> toRemove = new HashSet<>(currentRoleUuids);
        toRemove.removeAll(targetRoleUuids);
        if (!toRemove.isEmpty()) {
            int removed = userRoleRepository.deleteByUserUuidAndRoleUuidIn(user.getUuid(), toRemove);
            log.info(
                    "🧹 [UpmsUserRoleService] replaceRoles remove: user={}({}), removed={}",
                    user.getUsername(), user.getUuid(), removed
            );
        }

        // 7) 新增：存在於 target，但不在 current
        Set<UUID> toAdd = new LinkedHashSet<>(targetRoleUuids);
        toAdd.removeAll(currentRoleUuids);

        if (!toAdd.isEmpty()) {
            // 為避免再查一次 role，可用 roles list 做 mapping
            Map<UUID, UpmsRole> roleMap = new HashMap<>();
            for (UpmsRole r : roles) roleMap.put(r.getUuid(), r);

            List<UpmsUserRole> links = new ArrayList<>(toAdd.size());
            for (UUID roleUuid : toAdd) {
                UpmsRole role = roleMap.get(roleUuid);
                UpmsUserRole link = new UpmsUserRole();
                link.setUser(user);
                link.setRole(role);
                // link.setEffectiveFrom(LocalDateTime.now()); // 若你想預設生效時間
                links.add(link);
            }

            userRoleRepository.saveAll(links);
            log.info(
                    "✅ [UpmsUserRoleService] replaceRoles add: user={}({}), added={}",
                    user.getUsername(), user.getUuid(), links.size()
            );
        }

        log.info(
                "✅ [UpmsUserRoleService] replaceRoles done: user={}({}), targetCodes={}",
                user.getUsername(), user.getUuid(), normalizedTargetCodes
        );
    }

    // ===============================================================
    // Internal helpers - normalize / guard
    // ===============================================================

    private String normalizeRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ERR_ROLE_CODE_EMPTY, MSG_ROLE_CODE_EMPTY);
        }
        // 通常 role code 以大寫保存（你也可以改成 Locale.ROOT + trim）
        return roleCode.trim().toUpperCase(Locale.ROOT);
    }

    private LinkedHashSet<String> normalizeRoleCodes(Collection<String> roleCodes) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String code : roleCodes) {
            if (!StringUtils.hasText(code)) continue;
            out.add(code.trim().toUpperCase(Locale.ROOT));
        }
        return out;
    }

    private UpmsUser loadUserOrThrow(UUID userId) {
        if (userId == null) {
            throw new BusinessException(ERR_USER_ID_EMPTY, MSG_USER_ID_EMPTY);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ERR_USER_NOT_FOUND, MSG_USER_NOT_FOUND));
    }

    private UpmsRole loadRoleByCodeOrThrow(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ERR_ROLE_CODE_EMPTY, MSG_ROLE_CODE_EMPTY);
        }
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException(ERR_ROLE_NOT_FOUND, MSG_ROLE_NOT_FOUND));
    }
}
