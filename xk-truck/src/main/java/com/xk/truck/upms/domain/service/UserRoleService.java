package com.xk.truck.upms.domain.service;

import com.xk.truck.upms.domain.dao.repository.UpmsUserRoleRepository;

import com.xk.truck.upms.domain.model.UpmsUserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 用於管理「使用者 ↔ 角色」關聯
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

    private final UpmsUserRoleRepository userRoleRepository;

    public void assignRole(UUID userId, String roleCode) {
        UpmsUserRole relation = UpmsUserRole.builder()
                .userId(userId)
                .roleCode(roleCode)
                .build();

        // 若角色重複則略過（避免 Unique Key 衝突）
        userRoleRepository.findByUserIdAndRoleCode(userId, roleCode)
                .ifPresent(r -> { return; });

        userRoleRepository.save(relation);
    }
}
