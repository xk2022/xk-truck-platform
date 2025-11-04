package com.xk.truck.upms.domain.service;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.role.RoleCreateReq;
import com.xk.truck.upms.controller.api.dto.role.RoleResp;
import com.xk.truck.upms.controller.api.dto.role.RoleUpdateReq;
import com.xk.truck.upms.domain.dao.repository.PermissionRepository;
import com.xk.truck.upms.domain.dao.repository.RoleRepository;
import com.xk.truck.upms.domain.model.po.Permission;
import com.xk.truck.upms.domain.model.po.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ===============================================================
 * Service Class : RoleService
 * Layer         : Domain Service
 * Purpose       : 角色管理核心業務邏輯（CRUD、綁定權限）
 * Notes         :
 * - 未來可整合快取 (e.g. Redis) 或權限同步機制
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;

    public RoleResp create(RoleCreateReq req) {
        log.info("📌 [RoleService] 建立角色: {}", req.getCode());

        if (roleRepo.existsByCode(req.getCode())) {
            throw new BusinessException("ROLE_EXISTS", "角色代碼已存在");
        }

        Role role = XkBeanUtils.copyProperties(req, Role::new);
        if (req.getPermissionCodes() != null && !req.getPermissionCodes().isEmpty()) {
            Set<Permission> permissions = permRepo.findByCodeIn(req.getPermissionCodes());
            role.setPermissions(permissions);
        }

        Role saved = roleRepo.save(role);
        log.info("✅ 角色建立成功：{} ({})", saved.getCode(), saved.getName());

        return XkBeanUtils.copyProperties(saved, RoleResp::new);
    }

    public Page<RoleResp> list(Pageable pageable) {
        Page<Role> page = roleRepo.findAll(pageable);
        return page.map(r -> XkBeanUtils.copyProperties(r, RoleResp::new));
    }

    public RoleResp findById(UUID id) {
        Role role = roleRepo.findById(id)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "找不到角色"));
        return XkBeanUtils.copyProperties(role, RoleResp::new);
    }

    public RoleResp update(UUID id, RoleUpdateReq req) {
        Role role = roleRepo.findById(id)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "找不到角色"));

        if (req.getName() != null && !req.getName().isBlank()) {
            role.setName(req.getName());
        }

        if (req.getPermissionCodes() != null && !req.getPermissionCodes().isEmpty()) {
            Set<Permission> perms = permRepo.findByCodeIn(req.getPermissionCodes());
            role.setPermissions(perms);
        }

        Role saved = roleRepo.save(role);
        log.info("✏️ 更新角色成功：{}", saved.getCode());
        return XkBeanUtils.copyProperties(saved, RoleResp::new);
    }

    public void delete(UUID id) {
        if (!roleRepo.existsById(id)) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        roleRepo.deleteById(id);
        log.info("🗑️ 角色已刪除：{}", id);
    }

    public boolean existsByCode(String code) {
        return roleRepo.existsByCode(code);
    }
}
