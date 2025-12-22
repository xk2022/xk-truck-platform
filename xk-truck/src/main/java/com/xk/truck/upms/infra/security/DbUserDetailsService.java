package com.xk.truck.upms.infra.security;

import com.xk.truck.upms.controller.api.dto.role.UpmsRoleResp;
import com.xk.truck.upms.domain.model.*;
import com.xk.truck.upms.domain.repository.UpmsRolePermissionRepository;
import com.xk.truck.upms.domain.repository.UpmsUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UpmsUserRepository userRepository;
    private final UpmsRolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UpmsUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 1️⃣ User → UserRole → Role
        Set<UpmsRole> roles = user.getUserRoles().stream()
                .map(UpmsUserRole::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<UUID> roleUuids = roles.stream()
                .map(UpmsRole::getUuid) // 依你的 DTO 欄位調整
                .toList();

        // 2️⃣ Role → RolePermission → Permission
        List<UpmsRolePermission> rolePermissions = rolePermissionRepository.findByRoleUuidIn(roleUuids);

        Set<String> authorities = rolePermissions.stream()
                .map(UpmsRolePermission::getPermission)
                .map(UpmsPermission::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .accountLocked(Boolean.TRUE.equals(user.getLocked()))
                .authorities(authorities.toArray(String[]::new))
                .build();
    }
}
