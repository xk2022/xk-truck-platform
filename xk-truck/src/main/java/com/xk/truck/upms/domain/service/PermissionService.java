package com.xk.truck.upms.domain.service;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.permission.PermissionCreateReq;
import com.xk.truck.upms.controller.api.dto.permission.PermissionResp;
import com.xk.truck.upms.controller.api.dto.permission.PermissionUpdateReq;
import com.xk.truck.upms.domain.dao.repository.PermissionRepository;
import com.xk.truck.upms.domain.model.po.Permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ===============================================================
 * Service Class : PermissionService
 * Layer         : Domain Service
 * Purpose       : 權限管理核心邏輯（CRUD、代碼檢查、綁定角色）
 * Notes         :
 * - MVP 階段僅維護權限代碼與名稱描述
 * - 可後續整合角色與資源授權模組
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permRepo;

    public PermissionResp create(PermissionCreateReq req) {
        log.info("📌 [PermissionService] 建立權限: {}", req.getCode());

        if (permRepo.existsByCode(req.getCode())) {
            throw new BusinessException("PERMISSION_EXISTS", "權限代碼已存在");
        }

        Permission permission = XkBeanUtils.copyProperties(req, Permission::new);
        Permission saved = permRepo.save(permission);

        log.info("✅ 權限建立成功：{} ({})", saved.getCode(), saved.getName());
        return XkBeanUtils.copyProperties(saved, PermissionResp::new);
    }

    public Page<PermissionResp> list(Pageable pageable) {
        Page<Permission> page = permRepo.findAll(pageable);
        return page.map(p -> XkBeanUtils.copyProperties(p, PermissionResp::new));
    }

    public PermissionResp findById(UUID id) {
        Permission perm = permRepo.findById(id)
                .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND", "找不到權限"));
        return XkBeanUtils.copyProperties(perm, PermissionResp::new);
    }

    public PermissionResp update(UUID id, PermissionUpdateReq req) {
        Permission perm = permRepo.findById(id)
                .orElseThrow(() -> new BusinessException("PERMISSION_NOT_FOUND", "找不到權限"));

        if (req.getName() != null && !req.getName().isBlank()) {
            perm.setName(req.getName());
        }
        if (req.getDescription() != null && !req.getDescription().isBlank()) {
//            perm.setDescription(req.getDescription());
        }

        Permission saved = permRepo.save(perm);
        log.info("✏️ 更新權限成功：{}", saved.getCode());
        return XkBeanUtils.copyProperties(saved, PermissionResp::new);
    }

    public void delete(UUID id) {
        if (!permRepo.existsById(id)) {
            throw new BusinessException("PERMISSION_NOT_FOUND", "權限不存在");
        }
        permRepo.deleteById(id);
        log.info("🗑️ 權限已刪除：{}", id);
    }

    public boolean existsByCode(String code) {
        return permRepo.existsByCode(code);
    }
}
