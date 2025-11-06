package com.xk.truck.config;

import com.xk.truck.upms.controller.api.dto.user.UserCreateReq;
import com.xk.truck.upms.controller.api.dto.role.RoleCreateReq;
import com.xk.truck.upms.controller.api.dto.permission.PermissionCreateReq;
import com.xk.truck.upms.domain.service.UserService;
import com.xk.truck.upms.domain.service.RoleService;
import com.xk.truck.upms.domain.service.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * ===============================================================
 * Config Class : UpmsSeedConfig
 * Layer        : Configuration / Seed Initialization
 * Purpose      : 啟動時自動建立 UPMS 系統種子資料
 * ===============================================================
 * <p>
 * ✅ 功能說明：
 * - 初始化預設權限 → 初始化角色 → 初始化使用者
 * - 僅在 application.yml 中設定 upms.seed.enabled=true 時執行
 * - 避免重複建立（會檢查是否存在）
 * <p>
 * 📘 application.yml 設定範例：
 * upms:
 * seed:
 * enabled: true
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpmsSeedConfig {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    @ConditionalOnProperty(prefix = "upms.seed", name = "enabled", havingValue = "true")
    public CommandLineRunner seedUpmsData() {
        return args -> {
            log.info("🚀 [UPMS] 種子初始化開始...");

            // 1) Permissions
            seedPermission("USER_MANAGE", "使用者管理", "可新增、刪除、編輯使用者");
            seedPermission("ROLE_MANAGE", "角色管理", "可新增、刪除、編輯角色");
            seedPermission("PERMISSION_MANAGE", "權限管理", "可新增、刪除、編輯權限");
            seedPermission("TRUCK_MANAGE", "車輛管理", "可檢視與維護車輛資料");
            seedPermission("ORDER_MANAGE", "訂單管理", "可檢視與維護訂單資料");

            // 2) Roles
            seedRole("ADMIN", "系統管理員", Set.of(
                    "USER_MANAGE", "ROLE_MANAGE", "PERMISSION_MANAGE", "TRUCK_MANAGE", "ORDER_MANAGE"
            ));

            seedRole("DISPATCH", "調度人員", Set.of(
                    "TRUCK_MANAGE", "ORDER_MANAGE"
            ));

            seedRole("USER", "一般使用者", Set.of("ORDER_MANAGE"));

            // 3) Users (with BCrypt)
            seedUser("admin", "admin123", Set.of("ADMIN"));
            seedUser("dispatcher", "dispatch123", Set.of("DISPATCH"));

            log.info("✅ [UPMS] 種子資料初始化完成！");
        };
    }

    private void seedPermission(String code, String name, String desc) {
        if (permissionService.exists(code)) {
            log.info("✔ 权限已存在：{}", code);
            return;
        }
        var req = new PermissionCreateReq();
        req.setCode(code);
        req.setName(name);
        req.setDescription(desc);
        permissionService.create(req);
        log.info("🔑 建立 Permission：{}", code);
    }

    private void seedRole(String code, String name, Set<String> permissionCodes) {
        if (roleService.exists(code)) {
            log.info("✔ 角色已存在：{}", code);
            return;
        }
        var req = new RoleCreateReq();
        req.setCode(code);
        req.setName(name);
        req.setPermissionCodes(permissionCodes);
        roleService.create(req);
        log.info("👥 建立 Role：{}", code);
    }

    private void seedUser(String username, String rawPassword, Set<String> roleCodes) {
        if (userService.exists(username)) {
            log.info("✔ 使用者已存在：{}", username);
            return;
        }
        var req = new UserCreateReq();
        req.setUsername(username);
        req.setPassword(passwordEncoder.encode(rawPassword)); // ✅ 在這加密
        req.setRoleCodes(roleCodes);
        userService.create(req);
        log.info("👤 建立使用者：{}", username);
    }
}
