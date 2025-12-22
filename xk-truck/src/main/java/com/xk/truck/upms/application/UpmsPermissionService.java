package com.xk.truck.upms.application;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.permission.*;
import com.xk.truck.upms.domain.model.UpmsPermission;

import com.xk.truck.upms.domain.repository.UpmsPermissionRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    private static final String ERR_PERMISSION_NOT_FOUND = "UPMS_PERMISSION_NOT_FOUND";
    private static final String ERR_PERMISSION_EXISTS = "UPMS_PERMISSION_EXISTS";
    private static final String MSG_PERMISSION_NOT_FOUND = "找不到權限";
    private static final String MSG_PERMISSION_EXISTS = "權限代碼已存在";

    // ===============================================================
    // Repository / Collaborators
    // ===============================================================
    private final UpmsPermissionRepository permissionRepository;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立權限
     * <p>
     * 流程：
     * 1) 防呆 + normalize code
     * 2) 檢查 code 唯一（Service 層體驗，DB unique constraint 才是最後防線）
     * 3) 建立 entity（白名單欄位）
     * 4) save
     */
    public UpmsPermissionResp create(UpmsPermissionCreateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_PERMISSION_REQ_EMPTY", "建立權限請求不得為空");
        }

        final String normalizedCode = normalizeCode(req.getCode());
        if (!StringUtils.hasText(normalizedCode)) {
            throw new BusinessException("UPMS_PERMISSION_CODE_EMPTY", "權限代碼不能為空");
        }

        log.info("📌 [UpmsPermissionService] 建立權限: {}", normalizedCode);

        if (permissionRepository.existsByCode(normalizedCode)) {
            throw new BusinessException(ERR_PERMISSION_EXISTS, MSG_PERMISSION_EXISTS);
        }

        UpmsPermission permission = new UpmsPermission();
        XkBeanUtils.copyNonNullProperties(req, permission);

        // 覆蓋：code 一律以 normalize 後寫入
        permission.setCode(normalizedCode);

        // 預設 enabled（若你的 entity 已預設 true，這段只是保險）
        if (permission.getEnabled() == null) {
            permission.setEnabled(true);
        }

        UpmsPermission saved = permissionRepository.save(permission);
        log.info("✅ [UpmsPermissionService] 權限建立完成: {} ({})", saved.getCode(), saved.getUuid());

        return XkBeanUtils.copyProperties(saved, UpmsPermissionResp::new);
    }

    // ===============================================================
    // Read - Basic
    // ===============================================================

    @Transactional(readOnly = true)
    public UpmsPermissionResp findById(UUID id) {
        UpmsPermission p = loadPermissionOrThrow(id);
        return XkBeanUtils.copyProperties(p, UpmsPermissionResp::new);
    }

    @Transactional(readOnly = true)
    public UpmsPermissionResp findByCode(String code) {
        UpmsPermission p = loadPermissionByCodeOrThrow(code);
        return XkBeanUtils.copyProperties(p, UpmsPermissionResp::new);
    }

    // ===============================================================
    // Read - Page/List
    // ===============================================================

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
                    UpmsPermissionListResp dto = new UpmsPermissionListResp();
                    dto.setId(p.getUuid());
                    dto.setCode(p.getCode());
                    dto.setName(p.getName());
                    dto.setEnabled(p.getEnabled());
                    dto.setSortOrder(p.getSortOrder());   // 若沒有 sortOrder 欄位，可刪除此行

                    // systemCode 可能需要從關聯取（看你 entity）
                    // 若 UpmsPermission 有 systemCode 欄位：dto.setSystemCode(p.getSystemCode());
                    // 若 UpmsPermission 有 UpmsSystem system：dto.setSystemCode(p.getSystem().getCode());
                    dto.setSystemCode(extractSystemCodeSafely(p));

                    dto.setRemark(p.getRemark());
                    dto.setCreatedTime(p.getCreatedTime());
                    dto.setUpdatedTime(p.getUpdatedTime());
                    return dto;
                });
    }

    /**
     * 動態組合 Permission 查詢條件（Specification）
     * <p>
     * 規則（可依你的 query DTO 調整）：
     * - keyword：對 code/name like（lower + %keyword%）
     * - enabled：equal
     * - systemCode：若 entity 有 systemCode 欄位 -> equal；
     * 若是關聯 system -> join system
     * - type：equal（若你有）
     * <p>
     * ⚠ join 地雷：
     * - join 後要 cq.distinct(true) 避免重複 row
     * - count query 複雜化（資料量大可考慮 DTO query 或子查詢）
     */
    private Specification<UpmsPermission> buildPermissionSpec(UpmsPermissionQuery query) {
        return (root, cq, cb) -> {
            if (query == null) return cb.conjunction();

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // keyword (code/name)
            if (StringUtils.hasText(query.getKeyword())) {
                String kw = query.getKeyword().trim().toLowerCase(Locale.ROOT);
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("code")), "%" + kw + "%"),
                                cb.like(cb.lower(root.get("name")), "%" + kw + "%")
                        )
                );
            }

            // enabled
            if (query.getEnabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), query.getEnabled()));
            }

            // type（若你沒有 type 欄位，刪掉這段即可）
            if (StringUtils.hasText(query.getType())) {
                predicates.add(cb.equal(root.get("type"), query.getType().trim()));
            }

            // systemCode：兩種寫法，擇一（看你的 entity）
            if (StringUtils.hasText(query.getSystemCode())) {
                String sc = normalizeCode(query.getSystemCode());

                // A) 若 UpmsPermission 內是扁平欄位：private String systemCode;
                // predicates.add(cb.equal(cb.upper(root.get("systemCode")), sc));

                // B) 若 UpmsPermission 內是關聯：private UpmsSystem system;
                var systemJoin = root.join("system", JoinType.LEFT);
                predicates.add(cb.equal(cb.upper(systemJoin.get("code")), sc));
                cq.distinct(true);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
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
    public UpmsPermissionResp updateBasic(UUID id, UpmsPermissionUpdateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_PERMISSION_UPDATE_REQ_EMPTY", "更新資料請求不得為空");
        }

        UpmsPermission p = loadPermissionOrThrow(id);

        // ⚠ 防護：不允許更新 code（若你 DTO 有 code 欄位，這裡要忽略/擋掉）
        if (StringUtils.hasText(req.getCode())) {
            throw new BusinessException("UPMS_PERMISSION_CODE_IMMUTABLE", "權限代碼不允許修改");
        }

        XkBeanUtils.copyNonNullProperties(req, p);

        // dirty checking 會在 transaction commit 自動 flush
        log.info("✏️ [UpmsPermissionService] 權限更新完成: {} ({})", p.getCode(), p.getUuid());
        return XkBeanUtils.copyProperties(p, UpmsPermissionResp::new);
    }

    // ===============================================================
    // Status / Ops
    // ===============================================================

    /**
     * 啟用 / 停用
     * <p>
     * 說明：
     * - 使用 managed entity + dirty checking
     * - 若你想避免 session 依賴，可改呼叫 repository.updateEnabled(...)
     */
    public void updateEnabled(UUID id, boolean enabled) {
        UpmsPermission p = loadPermissionOrThrow(id);
        p.setEnabled(enabled);

        log.info("🔄 [UpmsPermissionService] 權限狀態更新: {} -> {}", p.getCode(), enabled ? "啟用" : "停用");
    }

    /**
     * 更新最後異動時間（範例：你若有類似欄位/需求）
     * - 這裡示範 bulk update 的寫法，避免拉 entity
     * - 若你 Repository 沒做 updateUpdatedTime，就不要用這支
     */
    public int touchUpdatedTime(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_PERMISSION_ID_EMPTY", "權限 ID 不得為空");
        }
        LocalDateTime now = LocalDateTime.now();
        // 你需要在 UpmsPermissionRepository 補一個 updateUpdatedTime 才能用
        // return permissionRepository.updateUpdatedTime(id, now);

        // 先保留示範（避免你沒建 Repository 方法導致 compile error）
        return 0;
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除權限
     * <p>
     * ⚠ 重要：權限通常會被 RolePermission 參照
     * - 若 DB 有 FK：你需要先刪 role_permission 關聯，再刪 permission
     * - 建議由 UpmsRolePermissionService 提供 clearByPermissionUuid(permissionUuid)
     * <p>
     * 這裡先做「只刪自身」的版本（低耦合），是否清關聯由外層 orchestrator 決定。
     */
    public void delete(UUID id) {
        UpmsPermission p = loadPermissionOrThrow(id);

        permissionRepository.deleteById(id);
        log.info("🗑️ [UpmsPermissionService] 權限已刪除: {} ({})", p.getCode(), p.getUuid());
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

    private UpmsPermission loadPermissionOrThrow(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_PERMISSION_ID_EMPTY", "權限 ID 不得為空");
        }
        return permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ERR_PERMISSION_NOT_FOUND, MSG_PERMISSION_NOT_FOUND));
    }

    private UpmsPermission loadPermissionByCodeOrThrow(String code) {
        String normalized = normalizeCode(code);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("UPMS_PERMISSION_CODE_EMPTY", "權限代碼不能為空");
        }
        return permissionRepository.findByCode(normalized)
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

    /**
     * 安全取得 systemCode（避免因為 mapping 不同而 Service 爆炸）
     * - 若你 Permission 沒有 system 關聯或 systemCode 欄位，可回 null
     */
    private String extractSystemCodeSafely(UpmsPermission p) {
        try {
            // 若你是關聯：p.getSystem().getCode()
            if (p.getSystemCode() != null && StringUtils.hasText(p.getSystemCode())) {
                return p.getSystemCode();
            }
        } catch (Exception ignore) {
            // 保持 service 穩定（避免 lazy initialization 例外）
        }
        // 若你是扁平欄位：return p.getSystemCode();
        return null;
    }
}
