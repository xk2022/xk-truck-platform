package com.xk.truck.upms.application;

import com.xk.base.domain.jpa.spec.EnabledSpec;
import com.xk.base.domain.jpa.spec.KeywordSpec;
import com.xk.base.domain.jpa.spec.SpecUtils;
import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.system.UpmsSystemCreateReq;
import com.xk.truck.upms.controller.api.dto.system.UpmsSystemListResp;
import com.xk.truck.upms.controller.api.dto.system.UpmsSystemQuery;
import com.xk.truck.upms.controller.api.dto.system.UpmsSystemResp;
import com.xk.truck.upms.controller.api.dto.system.UpmsSystemUpdateReq;
import com.xk.truck.upms.domain.model.UpmsSystem;
import com.xk.truck.upms.domain.repository.UpmsSystemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Service Class : UpmsSystemService
 * Layer         : Application / Domain Service
 * Purpose       : 系統模組（System）核心業務流程
 * Notes         :
 * - Repository 只做資料存取；Service 負責流程一致性（例外、驗證、去耦）
 * - 建議所有寫入操作皆走 @Transactional（class-level）
 * ===============================================================
 * <p>
 * ✔ 負責：
 * - 建立 System（代碼唯一）
 * - 查詢（by id / by code）
 * - 後台列表分頁查詢（Specification）
 * - 更新（基本資料）
 * - 啟用/停用（狀態）
 * - 刪除（若被 Permission/Role 依賴，應由更上層流程決定是否允許）
 * <p>
 * ❌ 不負責：
 * - Controller DTO 驗證（@Valid 等）
 * - Permission 與 System 的關聯維護（若要做「刪 System 連帶處理 Permission」，建議另開 UseCase）
 * <p>
 * ===============================================================
 * <p>
 * 設計原則（對齊你 UpmsUserService 風格）
 * 1) Error Code/Message 集中管理，避免到處打錯
 * 2) 重複的 findById + orElseThrow 抽成 Guard method
 * 3) System code 一律 normalize（避免 UPMS / upms / " upms " 變成不同系統）
 * 4) 唯一性檢查 + DB unique constraint 雙保險
 * 5) 查詢 readOnly、寫入 default transactional
 * <p>
 * ⚠ 排雷：
 * - updateBasicInternal() 使用 copyNonNullProperties：務必確保 UpdateReq 不含敏感欄位
 * - 若允許更新 code：要做 normalize + unique check（本實作採「允許但嚴格檢查」）
 * - pageForList() 若要避免 N+1（通常 System 不太有複雜關聯，可先忽略）
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpmsSystemService {

    // ===============================================================
    // Error Code / Message（集中管理，避免到處打錯）
    // ===============================================================
    private static final String ERR_SYSTEM_NOT_FOUND = "UPMS_SYSTEM_NOT_FOUND";
    private static final String MSG_SYSTEM_NOT_FOUND = "找不到系統";

    private static final String ERR_SYSTEM_EXISTS = "UPMS_SYSTEM_EXISTS";
    private static final String MSG_SYSTEM_EXISTS = "系統代碼已存在";

    // ===============================================================
    // Repository
    // ===============================================================
    private final UpmsSystemRepository systemRepository;

    // ===============================================================
    // Create
    // ===============================================================

    /**
     * 建立 System
     * <p>
     * 流程：
     * 1) req null guard
     * 2) normalize code
     * 3) unique check
     * 4) 建立 entity（建議白名單欄位）
     * 5) save
     */
    public UpmsSystemResp create(UpmsSystemCreateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_SYSTEM_REQ_EMPTY", "建立系統請求不得為空");
        }

        final String normalizedCode = UpmsSystem.normalizeCode(req.getCode());
        if (!StringUtils.hasText(normalizedCode)) {
            throw new BusinessException("UPMS_SYSTEM_CODE_EMPTY", "系統代碼不能為空");
        }

        log.info("📌 [UpmsSystemService] 建立系統: {}", normalizedCode);

        // 唯一性檢查（務必搭配 DB unique constraint）
        if (systemRepository.existsByCode(normalizedCode)) {
            throw new BusinessException(ERR_SYSTEM_EXISTS, MSG_SYSTEM_EXISTS);
        }

        UpmsSystem system = new UpmsSystem();
        // 你可以用 copyNonNullProperties，但建議把 code/敏感欄位顯式覆蓋
        XkBeanUtils.copyNonNullProperties(req, system);

        // code 一律使用 normalize 後寫入
        system.setCode(normalizedCode);

        // 預設值：enabled 若沒給，建議預設 true（視你的需求）
        if (system.getEnabled() == null) {
            system.setEnabled(true);
        }

        UpmsSystem saved = systemRepository.save(system);

        log.info("✅ [UpmsSystemService] 系統建立完成: {} ({})", saved.getCode(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsSystemResp::new);
    }

    // ===============================================================
    // Read
    // ===============================================================

    @Transactional(readOnly = true)
    public UpmsSystemResp findById(UUID id) {
        UpmsSystem system = loadSystemOrThrow(id);

        UpmsSystemResp resp = XkBeanUtils.copyProperties(system, UpmsSystemResp::new);
        resp.setId(system.getUuid());

        return resp;
    }

    @Transactional(readOnly = true)
    public UpmsSystemResp findByCode(String code) {
        UpmsSystem system = loadSystemByCodeOrThrow(code);
        return XkBeanUtils.copyProperties(system, UpmsSystemResp::new);
    }

    /**
     * 後台列表分頁查詢
     * <p>
     * ⚠ 注意：
     * - 本方法是「列表 DTO」輸出（UpmsSystemListResp）
     * - 若日後要加上 Permission 統計等欄位，建議用 DTO Projection Query 或額外查詢補齊
     */
    @Transactional(readOnly = true)
    public Page<UpmsSystemListResp> pageForList(UpmsSystemQuery query, Pageable pageable) {
        Specification<UpmsSystem> spec = null;

        spec = SpecUtils.and(spec, KeywordSpec.codeOrName(query.getKeyword()));
        spec = SpecUtils.and(spec, EnabledSpec.eq(query.getEnabled()));

        return systemRepository.findAll(spec, pageable)
                .map(system -> {
                    UpmsSystemListResp dto = XkBeanUtils.copyProperties(system, UpmsSystemListResp::new);
                    dto.setId(system.getUuid());
                    return dto;
                });
    }

    // ===============================================================
    // Update
    // ===============================================================

    /**
     * 更新基本資料（不含關聯處理）
     * <p>
     * 規則：
     * - req.code 若有提供：允許更新，但必須 normalize + unique check
     * - 其他欄位：copyNonNullProperties
     */
    public UpmsSystemResp updateBasic(UUID id, UpmsSystemUpdateReq req) {
        if (req == null) {
            throw new BusinessException("UPMS_SYSTEM_UPDATE_REQ_EMPTY", "更新系統請求不得為空");
        }

        UpmsSystem saved = updateBasicInternal(id, req);

        log.info("✏️ [UpmsSystemService] 系統更新完成: {} ({})", saved.getCode(), saved.getUuid());
        return XkBeanUtils.copyProperties(saved, UpmsSystemResp::new);
    }

    private UpmsSystem updateBasicInternal(UUID id, UpmsSystemUpdateReq req) {
        UpmsSystem system = loadSystemOrThrow(id);

        // 若允許更新 code：normalize + unique check
        if (StringUtils.hasText(req.getCode())) {
            String newCode = UpmsSystem.normalizeCode(req.getCode());
            if (!StringUtils.hasText(newCode)) {
                throw new BusinessException("UPMS_SYSTEM_CODE_EMPTY", "系統代碼不能為空");
            }

            if (!newCode.equals(system.getCode())) {
                if (systemRepository.existsByCode(newCode)) {
                    throw new BusinessException(ERR_SYSTEM_EXISTS, MSG_SYSTEM_EXISTS);
                }
                system.changeCode(newCode); // ✅ 建議用 domain method，避免外部 set 亂改
            }
        }

        // 其餘欄位：copy non-null（排雷：UpdateReq 不要包含 createdTime 等不該改欄位）
        XkBeanUtils.copyNonNullProperties(req, system);

        return systemRepository.save(system);
    }

    // ===============================================================
    // Status operations
    // ===============================================================

    /**
     * 啟用 / 停用 System
     * <p>
     * 這裡同 UpmsUserService：不一定要 save
     * - 因為 class-level @Transactional，managed entity 會在 commit flush
     * <p>
     * 如果你偏好 bulk update（避免 session/dirty checking）
     * 可在 UpmsSystemRepository 增加 updateEnabled(uuid, enabled)。
     */
    public void updateEnabled(UUID id, boolean enabled) {
        UpmsSystem system = loadSystemOrThrow(id);
        system.setEnabled(enabled);

        log.info("🔄 [UpmsSystemService] 系統狀態更新: {} -> {}", system.getCode(), enabled ? "啟用" : "停用");
    }

    /**
     * 更新排序（可選：若你後台有拖拉排序）
     * - 這裡示範 domain method + 時間戳
     */
    public void updateSortOrder(UUID id, Integer sortOrder) {
        if (sortOrder == null) {
            throw new BusinessException("UPMS_SYSTEM_SORT_EMPTY", "排序值不得為空");
        }

        UpmsSystem system = loadSystemOrThrow(id);
        system.setSortOrder(sortOrder);
        system.setUpdatedTime(ZonedDateTime.now());

        log.info("↕️ [UpmsSystemService] 系統排序更新: {} -> {}", system.getCode(), sortOrder);
    }

    // ===============================================================
    // Delete
    // ===============================================================

    /**
     * 刪除 System
     * <p>
     * ⚠ 排雷：System 通常會被 Permission 參照（FK / systemCode）
     * - 若 DB 有 FK：這裡可能會刪不掉
     * - 正確做法：
     * A) 禁止刪除：只能停用
     * B) 或由更上層 UseCase 做「先刪 Permission 再刪 System」
     * <p>
     * 這裡先維持「單純刪除」，讓你能依 DB constraint 決定策略。
     */
    public void delete(UUID id) {
        // guard
        loadSystemOrThrow(id);

        systemRepository.deleteById(id);
        log.info("🗑️ [UpmsSystemService] 系統已刪除: {}", id);
    }

    // ===============================================================
    // Validation / Exists
    // ===============================================================

    public boolean existsByCode(String code) {
        String normalized = UpmsSystem.normalizeCode(code);
        if (!StringUtils.hasText(normalized)) return false;
        return systemRepository.existsByCode(normalized);
    }

    // ===============================================================
    // Internal Guard / Loader
    // ===============================================================

    private UpmsSystem loadSystemOrThrow(UUID id) {
        if (id == null) {
            throw new BusinessException("UPMS_SYSTEM_ID_EMPTY", "系統 ID 不得為空");
        }
        return systemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ERR_SYSTEM_NOT_FOUND, MSG_SYSTEM_NOT_FOUND));
    }

    private UpmsSystem loadSystemByCodeOrThrow(String code) {
        String normalized = UpmsSystem.normalizeCode(code);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("UPMS_SYSTEM_CODE_EMPTY", "系統代碼不能為空");
        }
        return systemRepository.findByCode(normalized)
                .orElseThrow(() -> new BusinessException(ERR_SYSTEM_NOT_FOUND, MSG_SYSTEM_NOT_FOUND));
    }
}
