package com.xk.truck.upms.domain.service;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.upms.controller.api.dto.user.UserCreateReq;
import com.xk.truck.upms.controller.api.dto.user.UserResp;
import com.xk.truck.upms.controller.api.dto.user.UserUpdateReq;
import com.xk.truck.upms.domain.dao.repository.RoleRepository;
import com.xk.truck.upms.domain.dao.repository.UserRepository;
import com.xk.truck.upms.domain.model.po.Role;
import com.xk.truck.upms.domain.model.po.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ===============================================================
 * Service Class : UserService
 * Layer         : Domain Service
 * Purpose       : 使用者的核心業務操作（建立/查詢/啟用停用/重設密碼/指派角色）
 * Notes         :
 * - MVP 先回傳 Entity；未來可切換為 DTO + Mapper
 * - 建議所有寫入操作皆走 @Transactional
 * ===============================================================
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserResp create(UserCreateReq req) {
        log.info("📌 [UserService] 建立使用者: {}", req.getUsername());

        if (userRepo.existsByUsername(req.getUsername())) {
            throw new BusinessException("USER_EXISTS", "帳號已存在");
        }

        User user = XkBeanUtils.copyProperties(req, User::new);
        user.setPassword(encoder.encode(req.getPassword()));
        user.setEnabled(true);

        if (req.getRoleCodes() != null && !req.getRoleCodes().isEmpty()) {
            Set<Role> roles = roleRepo.findByCodeIn(req.getRoleCodes());
            user.setRoles(roles);
        }

        User saved = userRepo.save(user);
        log.info("✅ 建立成功：{} (roles={})", saved.getUsername(), saved.getRoles().size());

        return XkBeanUtils.copyProperties(saved, UserResp::new);
    }

    public Page<UserResp> list(Pageable pageable) {
        Page<User> page = userRepo.findAll(pageable);
        return page.map(u -> XkBeanUtils.copyProperties(u, UserResp::new));
    }

    public UserResp findById(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "找不到使用者"));
        return XkBeanUtils.copyProperties(user, UserResp::new);
    }

    public UserResp update(UUID id, UserUpdateReq req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "找不到使用者"));

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            user.setUsername(req.getUsername());
        }

        if (req.getRoleCodes() != null && !req.getRoleCodes().isEmpty()) {
            Set<Role> roles = roleRepo.findByCodeIn(req.getRoleCodes());
            user.setRoles(roles);
        }

        User saved = userRepo.save(user);
        log.info("✏️ 更新使用者成功：{}", saved.getUsername());
        return XkBeanUtils.copyProperties(saved, UserResp::new);
    }

    public UserResp enable(UUID id, boolean enabled) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "找不到使用者"));
        user.setEnabled(enabled);
        userRepo.save(user);
        log.info("🔄 使用者狀態更新：{} -> {}", user.getUsername(), enabled ? "啟用" : "停用");
        return XkBeanUtils.copyProperties(user, UserResp::new);
    }

    public void resetPassword(UUID id, String newPassword) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "找不到使用者"));
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        log.info("🔑 使用者密碼已重設：{}", user.getUsername());
    }

    public void delete(UUID id) {
        if (!userRepo.existsById(id)) {
            throw new BusinessException("USER_NOT_FOUND", "使用者不存在");
        }
        userRepo.deleteById(id);
        log.info("🗑️ 使用者已刪除：{}", id);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public boolean exists(String username) {
        return userRepo.existsByUsername(username);
    }
}
