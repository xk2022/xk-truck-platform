package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.truck.upms.domain.model.UpmsPermission;
import com.xk.truck.upms.domain.model.UpmsRole;
import com.xk.truck.upms.domain.model.UpmsRolePermission;
import com.xk.truck.upms.domain.repository.UpmsPermissionRepository;
import com.xk.truck.upms.domain.repository.UpmsRolePermissionRepository;
import com.xk.truck.upms.domain.repository.UpmsRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ===============================================================
 * Service Class : UpmsRolePermissionService
 * Layer         : Application / Domain Service
 * Purpose       : 角色 ↔ 權限 關聯管理（指派/覆蓋/增量同步/清除/查詢）
 * Notes         :
 * - Repository 僅做資料存取；Service 負責業務流程與一致性（例外、驗證、關聯維護）
 * - 角色與權限採用關聯實體 UpmsRolePermission（避免直接塞 Set<Permission> 的坑）
 * - 寫入操作必須在 Transaction 中執行（bulk delete/insert 也需）
 * <p>
 * ===============================================================
 * 設計原則（避免踩雷）
 * 1) 不讓 RoleService / PermissionService 直接操作中介表 Repository：統一收斂在這裡
 * 2) 不依賴 entity 的 equals/hashCode + Set 行為（避免 orphanRemoval/集合覆蓋踩雷）
 * 3) 覆蓋式指派採「差集同步」：只新增缺的、只刪除多的，減少 DB churn
 * 4) code 一律 normalize（trim + upper/lower 規範），避免大小寫造成重複資料/查不到
 * 5) DB Unique Constraint 必須存在（role_uuid + permission_uuid），Service 檢查只是提升體驗
 * <p>
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsRolePermissionService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_ROLE_NOT_FOUND = "UPMS_ROLE_NOT_FOUND";
    private static final String ERR_PERMISSION_NOT_FOUND = "UPMS_PERMISSION_NOT_FOUND";
    private static final String ERR_ROLE_PERMISSION_REQ_EMPTY = "UPMS_ROLE_PERMISSION_REQ_EMPTY";
    private static final String ERR_ROLE_ID_EMPTY = "UPMS_ROLE_ID_EMPTY";
    private static final String ERR_PERMISSION_CODES_EMPTY = "UPMS_PERMISSION_CODES_EMPTY";
    private static final String ERR_PERMISSION_CODE_EMPTY = "UPMS_PERMISSION_CODE_EMPTY";

    private static final String MSG_ROLE_NOT_FOUND = "找不到角色";
    private static final String MSG_PERMISSION_NOT_FOUND = "找不到權限";
    private static final String MSG_REQ_EMPTY = "請求不得為空";
    private static final String MSG_ROLE_ID_EMPTY = "角色 ID 不得為空";
    private static final String MSG_PERMISSION_CODES_EMPTY = "權限代碼集合不得為空";
    private static final String MSG_PERMISSION_CODE_EMPTY = "權限代碼不能為空";

    // ===============================================================
    // Repository / Collaborators
    // ===============================================================
    private final UpmsRolePermissionRepository rolePermissionRepository;
    private final UpmsRoleRepository roleRepository;
    private final UpmsPermissionRepository permissionRepository;

    // ===============================================================
    // Public APIs
    // ===============================================================

    /**
     * ✅ 覆蓋式指派（Replace）
     * <p>
     * 規則：
     * - targetCodes == null → 不做任何事（通常代表「不更新權限」）
     * - targetCodes != null 且 empty → 清空該角色所有權限
     * - 其餘 → 以差集同步（toAdd / toRemove）
     *
     * @param roleUuid    角色 UUID
     * @param targetCodes 權限代碼集合（可為 null / empty）
     */
    @Transactional
    public void replacePermissions(UUID roleUuid, Collection<String> permCodes) {

        UpmsRole role = loadRoleOrThrow(roleUuid);

        // 1) 查出 permission uuids（務必拿 entity uuid，不要只拿 code）
        List<UUID> permUuids = permissionRepository.findUuidsByCodes(permCodes);
        if (permUuids.isEmpty()) {
            log.warn("[replacePermissions] 查無任何對應的 Permission UUID，codes={}", permCodes);
            return;
        }
        if (permUuids.size() != permCodes.size()) {
            log.warn(
                    "[replacePermissions] permission code 數量與實際 UUID 數量不一致, codes={}, uuids={}",
                    permCodes.size(), permUuids.size()
            );
        }

        // 2) 先清掉舊關聯（用 roleUuid）
        rolePermissionRepository.deleteByRoleUuid(roleUuid);

        // 3) 建新關聯（重點：用 of(roleUuid, permUuid)）
        List<UpmsRolePermission> links = permUuids.stream()
                .distinct()
                .map(pid -> UpmsRolePermission.of(roleUuid, pid))
                .toList();


        rolePermissionRepository.saveAll(links);
    }
//    public void replacePermissions(UUID roleUuid, Collection<String> targetCodes) {
//        UpmsRole role = loadRoleOrThrow(roleUuid);
//
//        // null = caller 不想動權限
//        if (targetCodes == null) {
//            log.info("ℹ️ [UpmsRolePermissionService] replacePermissions skip (targetCodes is null), role={}", role.getCode());
//            return;
//        }
//
//        // normalize + 去空 + 去重（保留順序）
//        List<String> normalizedTargetCodes = normalizeCodesPreserveOrder(targetCodes);
//
//        // empty = 清空所有權限（這在後台很常用）
//        if (normalizedTargetCodes.isEmpty()) {
//            int removed = rolePermissionRepository.deleteByRoleUuid(role.getUuid());
//            log.info("🧹 [UpmsRolePermissionService] 清空角色權限: role={}, removed={}", role.getCode(), removed);
//            return;
//        }
//
//        // 1) 查出目標 permissions（一次查回）
//        List<UpmsPermission> targetPermissions =
//                permissionRepository.findAllByCodeIn(normalizedTargetCodes);
//
//        // 2) 驗證：是否有不存在的 code
//        //    - 這一步非常重要：避免「部分成功」造成權限資料不一致
//        Set<String> foundCodes = targetPermissions.stream()
//                .map(UpmsPermission::getCode)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//
//        List<String> missing = normalizedTargetCodes.stream()
//                .filter(c -> !foundCodes.contains(c))
//                .toList();
//
//        if (!missing.isEmpty()) {
//            throw new BusinessException(
//                    ERR_PERMISSION_NOT_FOUND,
//                    MSG_PERMISSION_NOT_FOUND + "：" + String.join(", ", missing)
//            );
//        }
//
//        // 3) 取得目前 role 已綁定的 permission UUIDs（避免拉整包關聯 entity）
//        Set<UUID> currentPermissionUuids = rolePermissionRepository.findPermissionUuidsByRoleUuid(role.getUuid());
//
//        // 4) 計算差集
//        Set<UUID> targetPermissionUuids = targetPermissions.stream()
//                .map(UpmsPermission::getUuid)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//
//        // toAdd = target - current
//        Set<UUID> toAdd = new LinkedHashSet<>(targetPermissionUuids);
//        toAdd.removeAll(currentPermissionUuids);
//
//        // toRemove = current - target
//        Set<UUID> toRemove = new LinkedHashSet<>(currentPermissionUuids);
//        toRemove.removeAll(targetPermissionUuids);
//
//        // 5) 刪除多餘的關聯（bulk）
//        if (!toRemove.isEmpty()) {
//            int removed = rolePermissionRepository.deleteByRoleUuidAndPermissionUuidIn(role.getUuid(), toRemove);
//            log.info("➖ [UpmsRolePermissionService] 移除角色權限: role={}, removed={}", role.getCode(), removed);
//        }
//
//        // 6) 新增缺少的關聯（逐筆 insert；也可用 saveAll）
//        if (!toAdd.isEmpty()) {
//            LocalDateTime now = LocalDateTime.now();
//            for (UUID permUuid : toAdd) {
//                UpmsRolePermission rp = new UpmsRolePermission();
//                rp.setRole(role);
//                rp.setPermission(createPermissionRef(permUuid));
//                // 若你的關聯表有 effectiveFrom/effectiveTo 等欄位，可在此設定
//                // rp.setEffectiveFrom(now);
//                rolePermissionRepository.save(rp);
//            }
//            log.info("➕ [UpmsRolePermissionService] 新增角色權限: role={}, added={}", role.getCode(), toAdd.size());
//        }
//
//        log.info(
//                "[UpmsRolePermissionService] replacePermissions 完成: role={}, target={}, add={}, remove={}",
//                role.getCode(), normalizedTargetCodes.size(), toAdd.size(), toRemove.size()
//        );
//    }

    /**
     * 增加單一權限（Add one）
     * - 若已存在：不報錯（可視需求改成報錯）
     */
    public void addPermission(UUID roleUuid, String permissionCode) {
        UpmsRole role = loadRoleOrThrow(roleUuid);
        String normalizedCode = normalizeSingleCodeOrThrow(permissionCode);

        UpmsPermission permission = loadPermissionByCodeOrThrow(normalizedCode);

        // 若 repository 有 existsByRoleUuidAndPermissionUuid 可先檢查避免 unique constraint exception
        boolean exists = rolePermissionRepository.existsByRoleUuidAndPermissionUuid(role.getUuid(), permission.getUuid());
        if (exists) {
            log.info(
                    "ℹ️ [UpmsRolePermissionService] addPermission skipped (already exists): role={}, perm={}",
                    role.getCode(), permission.getCode()
            );
            return;
        }

        UpmsRolePermission rp = new UpmsRolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rolePermissionRepository.save(rp);

        log.info("➕ [UpmsRolePermissionService] addPermission: role={}, perm={}", role.getCode(), permission.getCode());
    }

    /**
     * 移除單一權限（Remove one）
     * - 你之前提到缺漏的 deleteByRoleUuidAndPermissionUuid 就在這裡完整補上
     */
    public void removePermission(UUID roleUuid, UUID permissionUuid) {
        UpmsRole role = loadRoleOrThrow(roleUuid);
        if (permissionUuid == null) {
            throw new BusinessException("UPMS_PERMISSION_ID_EMPTY", "權限 ID 不得為空");
        }

        int affected = rolePermissionRepository.deleteByRoleUuidAndPermissionUuid(role.getUuid(), permissionUuid);
        log.info(
                "➖ [UpmsRolePermissionService] removePermission: role={}, permUuid={}, affected={}",
                role.getCode(), permissionUuid, affected
        );
    }

    /**
     * 清空角色所有權限（Clear）
     */
    public int clearPermissions(UUID roleUuid) {
        UpmsRole role = loadRoleOrThrow(roleUuid);
        int removed = rolePermissionRepository.deleteByRoleUuid(role.getUuid());
        log.info("🧹 [UpmsRolePermissionService] clearPermissions: role={}, removed={}", role.getCode(), removed);
        return removed;
    }

    /**
     * 查詢角色目前綁定的 permission UUIDs（給其他 Service/Controller 用）
     * - 不回 entity，降低耦合
     */
    @Transactional(readOnly = true)
    public Set<UUID> findPermissionUuidsByRoleUuid(UUID roleUuid) {
        UpmsRole role = loadRoleOrThrow(roleUuid);
        return rolePermissionRepository.findPermissionUuidsByRoleUuid(role.getUuid());
    }

    // ===============================================================
    // Internal Helpers / Normalization
    // ===============================================================

    /**
     * 權限代碼 normalize
     * - 你可以統一 upper 或 lower（依你的資料規範）
     * - 這裡採用 trim + upper，方便你像 SYS_ADMIN / USER_READ 這種 code
     */
    private static String normalizeCode(String code) {
        if (code == null) return null;
        String s = code.trim();
        if (s.isEmpty()) return null;
        return s.toUpperCase(Locale.ROOT);
    }

    private String normalizeSingleCodeOrThrow(String code) {
        String normalized = normalizeCode(code);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ERR_PERMISSION_CODE_EMPTY, MSG_PERMISSION_CODE_EMPTY);
        }
        return normalized;
    }

    private List<String> normalizeCodesPreserveOrder(Collection<String> codes) {
        if (codes == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String c : codes) {
            String n = normalizeCode(c);
            if (StringUtils.hasText(n)) set.add(n);
        }
        return new ArrayList<>(set);
    }

    /**
     * 當你只有 permissionUuid 但不想額外查一次 permission entity：
     * - 建立一個 reference（JPA managed proxy）
     * - 但 Spring Data JPA 無法直接 EntityManager.getReference
     * - 所以這裡用「只塞 uuid」的方式當作關聯（前提：你的 permission mapping 允許）
     * <p>
     * 若你堅持完全正規，請改成 permissionRepository.getReferenceById(uuid)
     * （Spring Data JPA 2.5+ 支援 getReferenceById）
     */
    private UpmsPermission createPermissionRef(UUID permissionUuid) {
        // 最推薦寫法（若你 Spring Data JPA 版本支援）
        try {
            return permissionRepository.getReferenceById(permissionUuid);
        } catch (Exception ignore) {
            // fallback：手動 new（若你的 mapping 不允許，請移除此段）
            UpmsPermission p = new UpmsPermission();
            p.setUuid(permissionUuid);
            return p;
        }
    }

    // ===============================================================
    // Guard methods（對齊 UpmsUserService 風格）
    // ===============================================================

    private UpmsRole loadRoleOrThrow(UUID roleUuid) {
        if (roleUuid == null) {
            throw new BusinessException(ERR_ROLE_ID_EMPTY, MSG_ROLE_ID_EMPTY);
        }
        return roleRepository.findById(roleUuid)
                .orElseThrow(() -> new BusinessException(ERR_ROLE_NOT_FOUND, MSG_ROLE_NOT_FOUND));
    }

    private UpmsPermission loadPermissionByCodeOrThrow(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ERR_PERMISSION_CODE_EMPTY, MSG_PERMISSION_CODE_EMPTY);
        }
        return permissionRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ERR_PERMISSION_NOT_FOUND, MSG_PERMISSION_NOT_FOUND + "：" + code));
    }
}
