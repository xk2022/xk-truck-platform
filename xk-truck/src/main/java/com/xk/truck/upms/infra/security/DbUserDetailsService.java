package com.xk.truck.upms.infra.security;

import com.xk.truck.upms.domain.dao.repository.UserRepository;
import com.xk.truck.upms.domain.model.po.Permission;
import com.xk.truck.upms.domain.model.po.Role;
import com.xk.truck.upms.domain.model.po.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true) // 確保 LAZY 關聯可讀取
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Roles -> ROLE_XXX
        Set<GrantedAuthority> roleAuths = u.getRoles().stream()
                .map(Role::getCode)
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toSet());

        // (選用) Permissions -> PERM_XXX
        Set<GrantedAuthority> permAuths = u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .map(code -> new SimpleGrantedAuthority("PERM_" + code))
                .collect(Collectors.toSet());

        Set<GrantedAuthority> authorities = Set.copyOf(
                new java.util.HashSet<GrantedAuthority>() {{
                    addAll(roleAuths);
                    addAll(permAuths);
                }}
        );

        log.debug(
                "[Auth] load user={}, enabled={}, roles={}, perms={}",
                u.getUsername(), u.getEnabled(),
                roleAuths.stream().map(GrantedAuthority::getAuthority).toList(),
                permAuths.stream().map(GrantedAuthority::getAuthority).toList()
        );

        // 這裡用 Spring 預設的 User 物件；若你有自訂 flags，改成 User.withUsername(...) builder
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())               // 必須是 BCrypt 後的值
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(Boolean.TRUE.equals(u.getLocked()))
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(u.getEnabled()))
                .build();
    }
}
