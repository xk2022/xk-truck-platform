package com.xk.truck.upms.application;

import com.xk.base.domain.jpa.spec.EnabledSpec;
import com.xk.base.domain.jpa.spec.KeywordSpec;
import com.xk.base.domain.jpa.spec.SpecUtils;
import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.role.*;
import com.xk.truck.upms.domain.model.UpmsRole;
import com.xk.truck.upms.domain.repository.UpmsRoleRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ===============================================================
 * Service Class : UpmsRoleService
 * Layer         : Application / Domain Service
 * Purpose       : 角色核心業務流程（建立/查詢/分頁/更新/啟用停用/刪除/權限指派）
 * Notes         :
 * - Repository 僅負責資料存取；Service 負責流程、一致性、例外、Guard、normalize
 * - Role code 一律 normalize（trim + upper），避免重複與資料不一致
 * - 權限指派請走 UpmsRolePermissionService（中介表），避免 RoleService 直接操作集合造成高耦合
 * <p>
 * ✔ 負責：
 * - 角色 CRUD
 * - 分頁查詢 / 規格查詢
 * - enabled 狀態切換
 * - 權限指派（委派給 RolePermissionService）
 * <p>
 * ❌ 不負責：
 * - Controller DTO 驗證（但 Service 仍保留最低限度防呆）
 * - Permission 的複雜查詢（由 PermissionService/Repository 處理）
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsRoleService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_ROLE_NOT_FOUND = "UPMS_ROLE_NOT_FOUND";
    private static final String ERR_ROLE_EXISTS = "UPMS_ROLE_EXISTS";
    private static final String ERR_ROLE_CODE_EMPTY = "UPMS_ROLE_CODE_EMPTY";
    private static final String ERR_ROLE_REQ_EMPTY = "UPMS_ROLE_REQ_EMPTY";

    private static final String MSG_ROLE_NOT_FOUND = "找不到角色";
    private static final String MSG_ROLE_EXISTS = "角色代碼已存在";
    private static final String MSG_ROLE_CODE_EMPTY = "角色代碼不能為空";
    private static final String MSG_ROLE_REQ_EMPTY = "請求不得為空";

    // ===============================================================
    // Repository / Collaborators
    // ===============================================================
    private final UpmsRoleRepository roleRepository;

    /**
     * 🔑 權限指派建議由 RolePermissionService 統一管理（低耦合）
     * - 你若目前還沒做，可先註解掉，或先建立空殼 Service
     */
    private final UpmsRolePermissionService rolePermissionService;

    // ===============================================================
    // Normalize utilities（關鍵：避免 code 重複 / 大小寫不一致）
    // ===============================================================

    /**
     * Role code normalize:
     * - trim
     * - toUpperCase
     */
    public static String normalizeRoleCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立角色（可選：帶 permissions 覆蓋式指派）
     * <p>
     * 流程：
     * 1) 防呆 req
     * 2) normalize code
     * 3) exists 檢查（同時 DB 也要 unique constraint，Service 檢查只是提升 UX）
     * 4) 建 entity（建議白名單欄位，避免 copy 亂塞敏感/關聯欄位）
     * 5) save
     * 6) 若 req.permissionCodes != null → 委派 rolePermissionService.replacePermissions(...)
     */
    public UpmsRoleResp create(UpmsRoleCreateReq req) {
        if (req == null) {
            throw new BusinessException(ERR_ROLE_REQ_EMPTY, MSG_ROLE_REQ_EMPTY);
        }

        final String normalizedCode = normalizeRoleCode(req.getCode());
        if (!StringUtils.hasText(normalizedCode)) {
            throw new BusinessException(ERR_ROLE_CODE_EMPTY, MSG_ROLE_CODE_EMPTY);
        }

        log.info("📌 [UpmsRoleService] 建立角色: {}", normalizedCode);

        if (roleRepository.existsByCode(normalizedCode)) {
            throw new BusinessException(ERR_ROLE_EXISTS, MSG_ROLE_EXISTS);
        }

        // ---- 建立 Role（建議白名單欄位）
        UpmsRole role = new UpmsRole();
        // 你若 UpmsRoleCreateReq 有很多欄位，可以用 copyNonNullProperties，但仍建議覆蓋 code
        XkBeanUtils.copyNonNullProperties(req, role);
        role.setCode(normalizedCode);

        // 若你 UpmsRole 有 enabled 預設 true 也 OK；這裡保留 req 優先
        // role.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);

        UpmsRole saved = roleRepository.save(role);

        // ---- 可選：指派 permissions（req.getPermissionCodes() != null → 覆蓋式指派）
        if (req.getPermissionCodes() != null) {
            rolePermissionService.replacePermissions(saved.getUuid(), req.getPermissionCodes());
        }

        log.info("✅ [UpmsRoleService] 角色建立完成: {} ({})", saved.getCode(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsRoleResp::new);
    }

    // ===============================================================
    // Read
    // ===============================================================

    @Transactional(readOnly = true)
    public UpmsRoleResp findById(UUID id) {
        UpmsRole role = loadRoleOrThrow(id);
        return XkBeanUtils.copyProperties(role, UpmsRoleResp::new);
    }

    @Transactional(readOnly = true)
    public UpmsRoleResp findByCode(String code) {
        UpmsRole role = loadRoleByCodeOrThrow(code);
        return XkBeanUtils.copyProperties(role, UpmsRoleResp::new);
    }

    /**
     * 分頁查詢（列表用）
     * - 目前採 findAll(spec, pageable) + map
     * - 若你要避免 N+1（例如 role 內有 permissions graph），建議改 DTO Query 或 Repository EntityGraph
     */
    @Transactional(readOnly = true)
    public Page<UpmsRoleListResp> pageForList(UpmsRoleQuery query, Pageable pageable) {
        Specification<UpmsRole> spec = null;

        spec = SpecUtils.and(spec, KeywordSpec.codeOrName(query.getKeyword()));
        spec = SpecUtils.and(spec, EnabledSpec.eq(query.getEnabled()));

        return roleRepository.findAll(spec, pageable)
                .map(role -> {
                    UpmsRoleListResp dto = new UpmsRoleListResp();
                    dto.setId(role.getUuid());
                    dto.setCode(role.getCode());
                    dto.setName(role.getName());
                    dto.setDescription(role.getDescription());
                    dto.setEnabled(role.getEnabled());

                    // 若 UpmsRole 有 systemCode/systemUuid/remark/sortOrder 等，可在這裡補
                    dto.setSortOrder(role.getSortOrder());
                    dto.setRemark(role.getRemark());

                    dto.setCreatedAt(role.getCreatedTime());
                    dto.setUpdatedAt(role.getUpdatedTime());
                    return dto;
                });
    }

    /**
     * 角色下拉選項（僅啟用中）
     * <p>
     * 用途：
     * - 使用者指派角色
     * - 後台下拉選單
     * <p>
     * 規則：
     * - enabled = true
     * - 排序：sortOrder asc → name asc → code asc
     */
    @Transactional(readOnly = true)
    public List<UpmsRoleOptionResp> options() {

        return roleRepository.findAll((root, cq, cb) -> {
                    cq.orderBy(
                            cb.asc(root.get("sortOrder")),
                            cb.asc(root.get("name")),
                            cb.asc(root.get("code"))
                    );
                    return cb.isTrue(root.get("enabled"));
                })
                .stream()
                .map(role -> {
                    UpmsRoleOptionResp dto = new UpmsRoleOptionResp();
                    dto.setId(role.getUuid());
                    dto.setCode(role.getCode());
                    dto.setName(role.getName());
                    return dto;
                })
                .toList();
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新角色基本資料（不含 permission 指派）
     * <p>
     * 規則：
     * - code 若允許更新：務必 normalize + unique check（建議通常不允許改 code）
     * - 建議 UpmsRoleUpdateReq 不含敏感欄位與關聯欄位，避免 copy 造成耦合污染
     */
    public UpmsRoleResp updateBasic(UUID id, UpmsRoleUpdateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_ROLE_UPDATE_REQ_EMPTY", "更新資料請求不得為空");
        }

        UpmsRole role = loadRoleOrThrow(id);

        // ---- 若你允許更新 code（通常不建議），務必做 normalize + unique check
        if (StringUtils.hasText(req.getCode())) {
            String newCode = normalizeRoleCode(req.getCode());
            if (!newCode.equals(role.getCode())) {
                if (roleRepository.existsByCode(newCode)) {
                    throw new BusinessException(ERR_ROLE_EXISTS, MSG_ROLE_EXISTS);
                }
                role.setCode(newCode);
            }
        }

        // ---- 其餘欄位 copy
        XkBeanUtils.copyNonNullProperties(req, role);

        // 走 dirty checking 或 save 都可；為一致性我保留 save（也較直觀）
        UpmsRole saved = roleRepository.save(role);

        log.info("✏️ [UpmsRoleService] 角色更新完成: {} ({})", saved.getCode(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsRoleResp::new);
    }

    /**
     * 覆蓋式更新（基本 + permissions）
     * <p>
     * 規則：
     * - req.permissionCodes == null → 不動 permissions
     * - req.permissionCodes != null → 覆蓋式 replacePermissions
     */
    public UpmsRoleResp updateAll(UUID id, UpmsRoleUpdateReq req) {
        UpmsRoleResp resp = updateBasic(id, req);

        if (req != null && req.getPermissionCodes() != null) {
            rolePermissionService.replacePermissions(id, req.getPermissionCodes());
        }

        return resp;
    }

    // ===============================================================
    // Status operations
    // ===============================================================

    /**
     * 啟用 / 停用
     * <p>
     * 說明：
     * - 這裡不一定要 save：transaction + dirty checking 足夠
     * - 若你想立即落庫，可改用 repository bulk update（你有骨架可做 updateEnabled）
     */
    public void updateEnabled(UUID id, boolean enabled) {
        UpmsRole role = loadRoleOrThrow(id);
        role.setEnabled(enabled);

        log.info("🔄 [UpmsRoleService] 角色狀態更新: {} -> {}", role.getCode(), enabled ? "啟用" : "停用");
    }

    // ===============================================================
    // Permission assignment（委派，低耦合）
    // ===============================================================

    /**
     * 覆蓋式指派角色權限（建議 Controller 直接打這支）
     * - 實作放在 UpmsRolePermissionService，RoleService 只負責流程與 guard
     */
    public void replacePermissions(UUID roleUuid, Collection<String> permissionCodes) {
        // guard role exists
        loadRoleOrThrow(roleUuid);
        rolePermissionService.replacePermissions(roleUuid, permissionCodes);
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除角色
     * <p>
     * 建議流程：
     * 1) guard role exists
     * 2) 清掉 role-permission 關聯（避免 FK constraint）
     * 3) delete
     * <p>
     * ⚠ 若 DB 有 FK：
     * - 一定要先刪中介表（UpmsRolePermission）
     * - 再刪 role
     */
    public void delete(UUID id) {
        UpmsRole role = loadRoleOrThrow(id);

        // 先清中介表（避免 FK constraint）
        rolePermissionService.clearPermissions(id);

        roleRepository.deleteById(id);
        log.info("🗑️ [UpmsRoleService] 角色已刪除: {} ({})", role.getCode(), role.getUuid());
    }

    // ===============================================================
    // Validation / Exists
    // ===============================================================

    public boolean existsByCode(String code) {
        String normalized = normalizeRoleCode(code);
        if (!StringUtils.hasText(normalized)) return false;
        return roleRepository.existsByCode(normalized);
    }

    // ===============================================================
    // Internal Guard / Loader
    // ===============================================================

    private UpmsRole loadRoleOrThrow(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_ROLE_ID_EMPTY", "角色 ID 不得為空");
        }
        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ERR_ROLE_NOT_FOUND, MSG_ROLE_NOT_FOUND));
    }

    private UpmsRole loadRoleByCodeOrThrow(String code) {
        String normalized = normalizeRoleCode(code);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ERR_ROLE_CODE_EMPTY, MSG_ROLE_CODE_EMPTY);
        }
        return roleRepository.findByCode(normalized)
                .orElseThrow(() -> new BusinessException(ERR_ROLE_NOT_FOUND, MSG_ROLE_NOT_FOUND));
    }
}
