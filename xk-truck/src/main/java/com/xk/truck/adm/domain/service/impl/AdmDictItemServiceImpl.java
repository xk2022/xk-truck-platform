package com.xk.truck.adm.domain.service.impl;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.adm.application.mapper.AdmDictItemMapper;
import com.xk.truck.adm.controller.api.dto.CreateDictItemReq;
import com.xk.truck.adm.controller.api.dto.DictItemResp;
import com.xk.truck.adm.controller.api.dto.SortPatchDictItemReq;
import com.xk.truck.adm.controller.api.dto.UpdateDictItemReq;
import com.xk.truck.adm.domain.model.AdmDictItem;
import com.xk.truck.adm.domain.repository.AdmDictCategoryRepository;

import com.xk.truck.adm.domain.repository.AdmDictItemRepository;
import com.xk.truck.adm.domain.service.AdmDictItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdmDictItemServiceImpl implements AdmDictItemService {

    private final AdmDictCategoryRepository categoryRepository;
    private final AdmDictItemRepository itemRepository;
    private final AdmDictItemMapper mapper;

    /* ==========================================================
     * Create
     * ========================================================== */

    @Override
    public DictItemResp create(UUID categoryId, @Valid CreateDictItemReq req) {
        final String itemCode = safeTrim(req.getItemCode());

        log.info("[AdmDictItemService] Create item start, categoryUuid={}, itemCode={}",
                categoryId, itemCode);

        // 1️⃣ category 必須存在
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("[AdmDictItemService] Category not found, categoryUuid={}", categoryId);
            throw new BusinessException("查無字典分類 id：" + categoryId);
        }

        // 2️⃣ itemCode 唯一（per category）
        if (itemRepository.existsByCategoryUuidAndItemCode(categoryId, itemCode)) {
            log.warn("[AdmDictItemService] Duplicate itemCode, categoryUuid={}, itemCode={}",
                    categoryId, itemCode);
            throw new BusinessException("字典項目 itemCode 已存在：" + itemCode);
        }

        // 3️⃣ DTO → Entity
        AdmDictItem entity = mapper.toEntity(req);
        entity.setCategoryUuid(categoryId);

        // 預設值
        if (entity.getEnabled() == null) entity.setEnabled(true);

        // sortOrder：自動補
        if (entity.getSortOrder() == null) {
            Integer next = itemRepository.findNextSortOrder(categoryId);
            entity.setSortOrder(next);
        }

        AdmDictItem saved = itemRepository.save(entity);

        log.info("[AdmDictItemService] Create item success, itemUuid={}, categoryUuid={}, itemCode={}",
                saved.getUuid(), categoryId, saved.getItemCode());

        return mapper.toResp(saved);
    }

    /* ==========================================================
     * Read
     * ========================================================== */

    @Override
    @Transactional(readOnly = true)
    public List<DictItemResp> findAllByCategoryId(UUID categoryId) {
        log.info("[AdmDictItemService] Find items, categoryUuid={}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException("查無字典分類 id：" + categoryId);
        }

        return mapper.toListResp(
                itemRepository.findAllByCategoryUuid(
                        categoryId,
                        Sort.by(
                                Sort.Order.asc("sortOrder"),
                                Sort.Order.asc("itemCode")
                        )
                )
        );
    }

    /* ==========================================================
     * Update
     * ========================================================== */

    @Override
    public DictItemResp update(UUID itemId, @Valid UpdateDictItemReq req) {
        log.info("[AdmDictItemService] Update item start, itemUuid={}", itemId);

        AdmDictItem entity = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("查無字典項目 id：" + itemId));

        UUID categoryUuid = entity.getCategoryUuid();

        // 若更新 itemCode，檢查唯一性
        if (req.getItemCode() != null && !req.getItemCode().isBlank()) {
            String newCode = safeTrim(req.getItemCode());

            if (!newCode.equals(entity.getItemCode())
                    && itemRepository.existsByCategoryUuidAndItemCode(categoryUuid, newCode)) {
                throw new BusinessException("字典項目 itemCode 已存在：" + newCode);
            }

            entity.setItemCode(newCode);
        }

        // Patch semantics
        XkBeanUtils.copyNonNullProperties(req, entity);

        AdmDictItem saved = itemRepository.save(entity);

        log.info("[AdmDictItemService] Update item success, itemUuid={}", saved.getUuid());

        return mapper.toResp(saved);
    }

    /* ==========================================================
     * Delete
     * ========================================================== */

    @Override
    public void delete(UUID itemId) {
        log.info("[AdmDictItemService] Delete item start, itemUuid={}", itemId);

        if (!itemRepository.existsById(itemId)) {
            throw new BusinessException("查無字典項目 id：" + itemId);
        }

        // MVP：硬刪
        itemRepository.deleteById(itemId);

        log.info("[AdmDictItemService] Delete item success, itemUuid={}", itemId);
    }

    /* ==========================================================
     * Sort
     * ========================================================== */

    @Override
    public void updateItemSort(@Valid SortPatchDictItemReq req) {
        UUID categoryUuid = req.getCategoryId();

        log.info("[AdmDictItemService] Batch sort start, categoryUuid={}, size={}",
                categoryUuid, req.getOrders().size());

        List<UUID> ids = req.getOrders().stream()
                .map(SortPatchDictItemReq.OrderPatch::getId)
                .toList();

        List<AdmDictItem> items = itemRepository.findAllById(ids);

        if (items.size() != ids.size()) {
            throw new BusinessException("部分字典項目不存在");
        }

        // 禁止跨分類
        boolean crossCategory = items.stream()
                .anyMatch(i -> !categoryUuid.equals(i.getCategoryUuid()));
        if (crossCategory) {
            throw new BusinessException("禁止跨分類更新排序");
        }

        var orderMap = req.getOrders().stream()
                .collect(Collectors.toMap(
                        SortPatchDictItemReq.OrderPatch::getId,
                        SortPatchDictItemReq.OrderPatch::getSortOrder
                ));

        for (AdmDictItem item : items) {
            item.setSortOrder(orderMap.get(item.getUuid()));
        }

        itemRepository.saveAll(items);

        log.info("[AdmDictItemService] Batch sort success, categoryUuid={}, count={}",
                categoryUuid, items.size());
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
