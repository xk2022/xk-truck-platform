package com.xk.truck.adm.domain.service.impl;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.adm.application.mapper.AdmDictCategoryMapper;
import com.xk.truck.adm.controller.api.dto.CreateDictCategoryReq;
import com.xk.truck.adm.controller.api.dto.DictCategoryResp;
import com.xk.truck.adm.controller.api.dto.UpdateDictCategoryReq;
import com.xk.truck.adm.domain.model.AdmDictCategory;
import com.xk.truck.adm.domain.repository.AdmDictCategoryRepository;
import com.xk.truck.adm.domain.service.AdmDictCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Service Class : AdmDictCategoryServiceImpl
 * Layer         : Application / Domain Service
 * Purpose       :
 * - 實作字典分類的核心業務邏輯
 * - 保證資料一致性與規則正確性
 * <p>
 * Key Responsibilities
 * - code 唯一性檢查
 * - Patch 更新（僅更新非 null 欄位）
 * - 刪除策略（目前為 MVP 硬刪）
 * <p>
 * Logging Guidelines（本類遵循）
 * - info : 主要操作的開始/成功（可追蹤誰做了什麼）
 * - warn : 可預期但不理想的狀況（如：重複 code、查無資料、刪除不存在）
 * - debug: 參數細節/變更欄位（避免噴太多到正式環境）
 * <p>
 * Notes
 * - 不處理 HTTP / Request / Response
 * - 不處理 DTO 驗證（@Valid 由 Controller 負責）
 * ===============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdmDictCategoryServiceImpl implements AdmDictCategoryService {

    private final AdmDictCategoryRepository categoryRepository;
    private final AdmDictCategoryMapper mapper;

    /* ==========================================================
     * Create
     * ========================================================== */

    @Override
    public DictCategoryResp create(CreateDictCategoryReq req) {
        final String code = req.getCode();

        log.info("[AdmDictCategoryService] Create category start, code={}", code);

        // 規則：code 必須唯一（DB Unique 約束仍是最終防線）
        if (categoryRepository.existsByCode(code)) {
            log.warn("[AdmDictCategoryService] Create category rejected (duplicate code), code={}", code);
            throw new BusinessException("字典分類 code 已存在：" + code);
        }

        AdmDictCategory entity = mapper.toEntity(req);
        AdmDictCategory saved = categoryRepository.save(entity);

        // 你專案 entity 似乎用 uuid 欄位（saved.getUuid()），保留你的命名
        log.info(
                "[AdmDictCategoryService] Create category success, id={}, code={}",
                saved.getUuid(), saved.getCode()
        );

        if (log.isDebugEnabled()) {
            log.debug(
                    "[AdmDictCategoryService] Create category detail, enabled={}, name={}, remark={}",
                    saved.getEnabled(), saved.getName(), saved.getRemark()
            );
        }

        return mapper.toResp(saved);
    }

    /* ==========================================================
     * Read
     * ========================================================== */

    @Override
    @Transactional(readOnly = true)
    public DictCategoryResp findByCode(String code) {
        log.info("[AdmDictCategoryService] Find category by code start, code={}", code);

        AdmDictCategory entity = categoryRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("[AdmDictCategoryService] Find category by code not found, code={}", code);
                    return new BusinessException("查無字典分類：" + code);
                });

        log.info(
                "[AdmDictCategoryService] Find category by code success, id={}, code={}",
                entity.getUuid(), entity.getCode()
        );

        return mapper.toResp(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictCategoryResp> findAll() {
        log.info("[AdmDictCategoryService] Find all categories start");

        List<AdmDictCategory> entities = categoryRepository.findAll(
                Sort.by(Sort.Direction.ASC, "code")
        );

        log.info("[AdmDictCategoryService] Find all categories success, count={}", entities.size());

        return mapper.toListResp(entities);
    }

    /* ==========================================================
     * Update
     * ========================================================== */

    @Override
    public DictCategoryResp update(UUID id, UpdateDictCategoryReq req) {
        log.info("[AdmDictCategoryService] Update category start, id={}", id);

        AdmDictCategory entity = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[AdmDictCategoryService] Update category not found, id={}", id);
                    return new BusinessException("查無字典分類 id：" + id);
                });

        // Debug：更新前快照（避免 info 太吵）
        if (log.isDebugEnabled()) {
            log.debug(
                    "[AdmDictCategoryService] Update category before, id={}, code={}, name={}, enabled={}, remark={}",
                    entity.getUuid(), entity.getCode(), entity.getName(), entity.getEnabled(), entity.getRemark()
            );
        }

        // code 若更新：檢查唯一性
        if (req.getCode() != null && !req.getCode().isBlank()) {
            String newCode = req.getCode().trim();

            if (!newCode.equals(entity.getCode()) && categoryRepository.existsByCode(newCode)) {
                log.warn(
                        "[AdmDictCategoryService] Update category rejected (duplicate code), id={}, oldCode={}, newCode={}",
                        id, entity.getCode(), newCode
                );
                throw new BusinessException("字典分類 code 已存在：" + newCode);
            }

            // 先 set code（你原本邏輯保留）
            entity.setCode(newCode);
        }

        // Patch semantics：只複製非 null
        XkBeanUtils.copyNonNullProperties(req, entity);

        AdmDictCategory saved = categoryRepository.save(entity);

        log.info(
                "[AdmDictCategoryService] Update category success, id={}, code={}",
                saved.getUuid(), saved.getCode()
        );

        if (log.isDebugEnabled()) {
            log.debug(
                    "[AdmDictCategoryService] Update category after, id={}, code={}, name={}, enabled={}, remark={}",
                    saved.getUuid(), saved.getCode(), saved.getName(), saved.getEnabled(), saved.getRemark()
            );
        }

        return mapper.toResp(saved);
    }

    /* ==========================================================
     * Delete
     * ========================================================== */

    @Override
    public void delete(UUID id) {
        log.info("[AdmDictCategoryService] Delete category start, id={}", id);

        // 存在性檢查（避免 deleteById 靜默不動）
        if (!categoryRepository.existsById(id)) {
            log.warn("[AdmDictCategoryService] Delete category not found, id={}", id);
            throw new BusinessException("查無字典分類 id：" + id);
        }

        // MVP：硬刪（正式環境建議：改停用或檢查是否有 items / 是否被引用）
        categoryRepository.deleteById(id);

        log.info("[AdmDictCategoryService] Delete category success, id={}", id);
    }
}
