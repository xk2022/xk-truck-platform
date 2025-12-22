package com.xk.truck.adm.domain.service;

import com.xk.truck.adm.domain.model.DictCategory;
import com.xk.truck.adm.domain.model.DictItem;
import com.xk.truck.adm.domain.repo.DictCategoryRepository;

import com.xk.truck.adm.domain.repo.DictItemRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictCategoryRepository catRepo;
    private final DictItemRepository itemRepo;

    // Category
    @Transactional
    public DictCategory createCategory(DictCategory req) {
        // 可加：防重 catRepo.findByCode(...)
        return catRepo.save(req);
    }

    public DictCategory getCategory(UUID id) {
        return catRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("DictCategory not found"));
    }

    // Item
    @Transactional
    public DictItem createItem(UUID categoryId, DictItem req) {
        DictCategory cat = getCategory(categoryId);
        req.setCategory(cat);
        // 可加：existsByCategory_IdAndCode(categoryId, req.getCode())
        return itemRepo.save(req);
    }

    public List<DictItem> listEnabledItemsByCode(String categoryCode) {
        return itemRepo.findByCategory_CodeAndEnabledIsTrueOrderBySortNoAsc(categoryCode);
    }
}
