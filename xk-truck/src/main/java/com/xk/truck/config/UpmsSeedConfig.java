package com.xk.truck.config;

import com.xk.base.exception.BusinessException;
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

    @Bean
    @Transactional
    @ConditionalOnProperty(prefix = "upms.seed", name = "enabled", havingValue = "true")
    public CommandLineRunner seedUpmsData() {
        return args -> {
            log.info("🚀 [UPMS] 初始化種子資料開始...");

            // =============================================================
            // 1️⃣ 初始化權限 (Permissions)
            // =============================================================
            createPermission("USER_MANAGE", "使用者管理", "可新增、刪除、編輯使用者");
            createPermission("ROLE_MANAGE", "角色管理", "可新增、刪除、編輯角色");
            createPermission("PERMISSION_MANAGE", "權限管理", "可新增、刪除、編輯權限");
            createPermission("TRUCK_MANAGE", "車輛管理", "可檢視與維護車輛資料");
            createPermission("ORDER_MANAGE", "訂單管理", "可檢視與維護訂單資料");

            // =============================================================
            // 2️⃣ 初始化角色 (Roles)
            // =============================================================
            createRole(
                    "ADMIN", "系統管理員", Set.of(
                            "USER_MANAGE", "ROLE_MANAGE", "PERMISSION_MANAGE", "TRUCK_MANAGE", "ORDER_MANAGE")
            );

            createRole(
                    "DISPATCH", "調度人員", Set.of(
                            "TRUCK_MANAGE", "ORDER_MANAGE")
            );

            createRole("USER", "一般使用者", Set.of("ORDER_MANAGE"));

            // =============================================================
            // 3️⃣ 初始化使用者 (Users)
            // =============================================================
            createUser("admin", "admin123", Set.of("ADMIN"));
            createUser("dispatcher", "dispatcher123", Set.of("DISPATCH"));

            log.info("✅ [UPMS] 種子資料初始化完成！");
        };
    }

    // =============================================================
    // 權限建立工具
    // =============================================================
    private void createPermission(String code, String name, String desc) {
        if (permissionService.existsByCode(code)) {
            log.debug("⚠️ 權限已存在：{}", code);
            return;
        }
        PermissionCreateReq req = new PermissionCreateReq();
        req.setCode(code);
        req.setName(name);
        req.setDescription(desc);
        permissionService.create(req);
        log.info("🔑 建立權限：{} ({})", code, name);
    }

    // =============================================================
    // 角色建立工具
    // =============================================================
    private void createRole(String code, String name, Set<String> permCodes) {
        if (roleService.existsByCode(code)) {
            log.debug("⚠️ 角色已存在：{}", code);
            return;
        }
        var req = new RoleCreateReq();
        req.setCode(code);
        req.setName(name);
        req.setPermissionCodes(permCodes);
        roleService.create(req);
        log.info("👥 建立角色：{} ({})", code, name);
    }

    // =============================================================
    // 使用者建立工具
    // =============================================================
    private void createUser(String username, String password, Set<String> roleCodes) {
        if (userService.existsByUsername(username)) {
            log.debug("⚠️ 使用者已存在：{}", username);
            return;
        }
        var req = new UserCreateReq();
        req.setUsername(username);
        req.setPassword(password);
        req.setRoleCodes(roleCodes);
        userService.create(req);
        log.info("👤 建立使用者：{}", username);
    }
}
