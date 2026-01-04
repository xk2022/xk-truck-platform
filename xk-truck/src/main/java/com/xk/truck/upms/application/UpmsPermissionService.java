package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.permission.*;
import com.xk.truck.upms.domain.model.UpmsPermission;

import com.xk.truck.upms.domain.repository.UpmsPermissionRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * ===============================================================
 * Service Class : UpmsPermissionService
 * Layer         : Application / Domain Service
 * Purpose       : 權限核心業務流程（建立/查詢/分頁/啟用停用/刪除）
 * Notes         :
 * - MVP 階段僅維護權限代碼與名稱描述
 * - 可後續整合角色與資源授權模組
 * - Repository 只做資料存取；Service 負責業務流程與一致性（例外、驗證、規範化、關聯維護）
 * - 建議所有寫入操作皆走 @Transactional（class-level）
 * ===============================================================
 * <p>
 * 設計原則（避免踩雷 / 低耦合）
 * 1) Permission 以「code」作為唯一識別（搭配 DB unique constraint），Service 只做體驗檢查，DB 才是最後防線
 * 2) code 一律 normalize（trim + upper），避免 "order.read" vs "ORDER.READ" 變成兩筆
 * 3) 不在 Service 內直接碰 RolePermission 關聯（由 UpmsRolePermissionService 管），避免耦合擴散
 * 4) 讀取：@Transactional(readOnly = true)；寫入：預設 @Transactional
 * 5) Specification 查詢避免硬寫多支 Query，後續可擴充更多條件
 * <p>
 * ⚠ 注意
 * - 若 Permission 和 System 有關聯（ManyToOne system），pageForList() 可能有 N+1
 * 建議搭配 Repository 的 EntityGraph 或 DTO Query。
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsPermissionService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_PERMISSION_REQ_EMPTY = "UPMS_PERMISSION_REQ_EMPTY";
    private static final String MSG_PERMISSION_REQ_EMPTY = "請求不得為空";

    private static final String ERR_PERMISSION_NOT_FOUND = "UPMS_PERMISSION_NOT_FOUND";
    private static final String MSG_PERMISSION_NOT_FOUND = "找不到指定權限";

    private static final String ERR_PERMISSION_EXISTS = "UPMS_PERMISSION_EXISTS";
    private static final String MSG_PERMISSION_EXISTS = "權限已存在";

    // ===============================================================
    // Repository / Collaborators
    // ===============================================================
    private final UpmsPermissionRepository permissionRepository;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立權限（平台級）
     * <p>
     * 流程：
     * 1) 驗證請求
     * 2) 檢查 (system + resource + action) 唯一性
     * 3) 透過 Domain Factory 建立 Permission
     * 4) 補充可變欄位（白名單）
     * 5) save
     */
    @Transactional
    public UpmsPermissionResp create(UpmsPermissionCreateReq req) {
        if (req == null) {
            throw new BusinessException(ERR_PERMISSION_REQ_EMPTY, "建立權限" + MSG_PERMISSION_REQ_EMPTY);
        }

        final String systemCode = UpmsPermission.normalizeCode(req.getSystemCode());
        final String resourceCode = UpmsPermission.normalizeCode(req.getResourceCode());
        final String actionCode = UpmsPermission.normalizeCode(req.getActionCode());

        log.info("📌 [UpmsPermissionService] 建立權限: {}_{}_{}", systemCode, resourceCode, actionCode);

        // 語意唯一性檢查：system + resource + action（DB unique constraint 是最後防線）
        if (permissionRepository.existsBySystemCodeAndResourceCodeAndActionCode(systemCode, resourceCode, actionCode)) {
            throw new BusinessException(ERR_PERMISSION_EXISTS, MSG_PERMISSION_EXISTS);
        }

        // Domain Factory（唯一允許產生 code 的地方）
        UpmsPermission permission = UpmsPermission.create(
                systemCode,
                resourceCode,
                actionCode,
                req.getName()
        );

        // 白名單可變欄位
        permission.setDescription(req.getDescription());
        permission.setEnabled(req.getEnabled() != null ? req.getEnabled() : Boolean.TRUE);
        permission.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        // UI helper（若你的 entity create() 已經設定 groupKey，可移除）
        if (!StringUtils.hasText(permission.getGroupKey())) {
            permission.setGroupKey(systemCode + "_" + resourceCode);
        }

        UpmsPermission saved = permissionRepository.save(permission);

        log.info("[UpmsPermissionService] 權限建立完成: {} ({})", saved.getCode(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsPermissionResp::new);
    }

    // ===============================================================
    // Read
    // ===============================================================

    @Transactional(readOnly = true)
    public UpmsPermissionResp findById(UUID id) {
        UpmsPermission p = loadOrThrow(id);
        return XkBeanUtils.copyProperties(p, UpmsPermissionResp::new);
    }

    /**
     * 後台列表分頁
     * <p>
     * ⚠ 若你 Permission 有關聯 system（LAZY）且你這裡 mapping 會讀到 system，
     * 可能 N+1。建議：
     * - Repository 提供 findAllWithSystem(...) @EntityGraph
     * - 或 DTO Query：select new UpmsPermissionListResp(...)
     */
    @Transactional(readOnly = true)
    public Page<UpmsPermissionListResp> pageForList(UpmsPermissionQuery query, Pageable pageable) {
        Specification<UpmsPermission> spec = buildPermissionSpec(query);

        return permissionRepository.findAll(spec, pageable)
                .map(p -> {
                    UpmsPermissionListResp dto = XkBeanUtils.copyProperties(p, UpmsPermissionListResp::new);
                    dto.setId(p.getUuid());
                    return dto;
                });
    }

    // ===============================================================
    // Update - Basic
    // ===============================================================

    /**
     * 更新基本資料（不改 code）
     * <p>
     * 排雷：
     * - code 視為 immutable（強烈建議），避免關聯表/外部引用全壞
     * - 若你真的要改 code，請做專門的 renameCode()，並在 DB/外部系統同步
     */
    @Transactional
    public UpmsPermissionResp update(UUID id, UpmsPermissionUpdateReq req) {
        if (req == null) {
            throw new BusinessException(ERR_PERMISSION_REQ_EMPTY, "更新權限" + MSG_PERMISSION_REQ_EMPTY);
        }

        UpmsPermission permission = loadOrThrow(id);

        // 白名單欄位：允許 partial update（null = 不改）
//        if (StringUtils.hasText(req.getName())) {
//            permission.setName(req.getName().trim());
//        }
//        if (req.getDescription() != null) {
//            permission.setDescription(req.getDescription());
//        }
//        if (req.getEnabled() != null) {
//            permission.setEnabled(req.getEnabled());
//        }
//        if (req.getSortOrder() != null) {
//            permission.setSortOrder(req.getSortOrder());
//        }
        XkBeanUtils.copyNonNullProperties(req, permission);

        UpmsPermission saved = permissionRepository.save(permission);
        log.info("[UpmsPermissionService] 權限更新完成: {} ({})", saved.getCode(), saved.getUuid());

        return XkBeanUtils.copyProperties(saved, UpmsPermissionResp::new);
    }

    // ============================================================
    // Delete (soft delete)
    // ============================================================
    @Transactional
    public void delete(UUID id) {
        UpmsPermission p = loadOrThrow(id);

        // idempotent：已刪除就不重複寫（可改成 throw，看你的政策）
        if (p.getDeletedAt() != null) {
            log.info("🗑️ [UpmsPermissionService] 權限已是刪除狀態: {} ({})", p.getCode(), p.getUuid());
            return;
        }

        p.setDeletedAt(Instant.now());
        permissionRepository.save(p);

        log.info("🗑️ [UpmsPermissionService] 權限已刪除: {} ({})", p.getCode(), p.getUuid());
    }

    // ============================================================
    // Specification builder
    // ============================================================
    @Transactional(readOnly = true)
    private Specification<UpmsPermission> buildPermissionSpec(UpmsPermissionQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 永遠排除軟刪
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (query == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // keyword (code/name)
            String kw = query.getKeyword();
            if (StringUtils.hasText(kw)) {
                String like = "%" + kw.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("code")), like),
                                cb.like(cb.lower(root.get("name")), like)
                        )
                );
            }

            // enabled
            if (query.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), query.getEnabled()));
            }

            // systemCode（目前 entity 是扁平欄位，不要 join）
            String systemCode = query.getSystemCode();
            if (StringUtils.hasText(systemCode)) {
                String sc = UpmsPermission.normalizeCode(systemCode);
                predicates.add(cb.equal(root.get("systemCode"), sc));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ===============================================================
    // Validation / Exists
    // ===============================================================

    public boolean existsByCode(String code) {
        String normalized = normalizeCode(code);
        if (!StringUtils.hasText(normalized)) return false;
        return permissionRepository.existsByCode(normalized);
    }

    // ===============================================================
    // Internal Guard / Loader
    // ===============================================================

    private UpmsPermission loadOrThrow(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_PERMISSION_ID_EMPTY", "權限 UUID 不得為空");
        }
        return permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ERR_PERMISSION_NOT_FOUND, MSG_PERMISSION_NOT_FOUND));
    }

    // ===============================================================
    // Helpers - Normalize / Safe extract
    // ===============================================================

    /**
     * 代碼規範化：trim + upper
     * - 避免 "sys.user.read" / "SYS.USER.READ" 變成兩筆
     * - 若你希望保留小寫：改成 lower 即可，但要「全系統一致」
     */
    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) return null;
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
