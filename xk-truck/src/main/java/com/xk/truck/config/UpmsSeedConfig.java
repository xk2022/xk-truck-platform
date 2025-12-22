package com.xk.truck.config;

import com.xk.truck.upms.application.*;

import com.xk.truck.upms.controller.api.dto.permission.UpmsPermissionCreateReq;

import com.xk.truck.upms.controller.api.dto.permission.UpmsPermissionListResp;
import com.xk.truck.upms.controller.api.dto.role.UpmsRoleCreateReq;

import com.xk.truck.upms.controller.api.dto.role.UpmsRoleResp;
import com.xk.truck.upms.controller.api.dto.user.UpmsUserCreateReq;

import com.xk.truck.upms.controller.api.dto.user.UpmsUserResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ===============================================================
 * Config Class : UpmsSeedConfig
 * Layer        : Configuration / Seed Initialization
 * Purpose      : 啟動時自動建立 UPMS 系統種子資料
 * ===============================================================
 * <p>
 * 功能說明：
 * - 初始化預設權限 → 初始化角色 → 初始化使用者
 * - 僅在 application.yml 中設定 upms.seed.enabled=true 時執行
 * - 避免重複建立（會檢查是否存在）
 * <p>
 * application.yml 設定範例：
 * upms:
 * seed:
 * enabled: true
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpmsSeedConfig {

    private final UpmsUserService userService;
    private final UpmsRoleService roleService;
    private final UpmsPermissionService permissionService;
    private final UpmsUserRoleService userRoleService;
    private final UpmsRolePermissionService rolePermissionService;

    @Bean
    @Transactional
    @ConditionalOnProperty(prefix = "upms.seed", name = "enabled", havingValue = "true")
    public CommandLineRunner seedUpmsData() {
        return args -> {
            log.info("[UPMS] 種子初始化開始...");

            // 1) Permissions
            seedPermission("UPMS", "UPMS_USER", "使用者管理", "車隊平台用戶資料管理");
            seedPermission("UPMS", "UPMS_USER_VIEW", "使用者管理V", "");
            seedPermission("UPMS", "UPMS_USER_CREATE", "使用者管理C", "");
            seedPermission("UPMS", "UPMS_USER_UPDATE", "使用者管理U", "");
            seedPermission("UPMS", "UPMS_USER_DELETE", "使用者管理D", "");
            seedPermission("UPMS", "UPMS_USER_RESET_PWD", "使用者管理R", "");
            seedPermission("UPMS", "UPMS_USER_ENABLE", "使用者管理E", "");
            seedPermission("UPMS", "UPMS_ROLE", "角色管理", "可維護角色與權限");
            seedPermission("UPMS", "UPMS_ROLE_VIEW", "角色管理V", "");
            seedPermission("UPMS", "UPMS_ROLE_CREATE", "角色管理C", "");
            seedPermission("UPMS", "UPMS_ROLE_UPDATE", "角色管理U", "");
            seedPermission("UPMS", "UPMS_ROLE_DELETE", "角色管理D", "");
            seedPermission("UPMS", "UPMS_ROLE_ASSIGN_PERM", "角色管理A", "");
            seedPermission("FMS", "FMS_TRUCK", "車輛資料", "車輛資料");
            seedPermission("FMS", "FMS_TRUCK_VIEW", "車輛資料V", "");
            seedPermission("FMS", "FMS_TRUCK_CREATE", "車輛資料C", "");
            seedPermission("FMS", "FMS_TRUCK_UPDATE", "車輛資料U", "");
            seedPermission("FMS", "FMS_TRUCK_DELETE", "車輛資料D", "");
            seedPermission("FMS", "FMS_DRIVER", "司機列表", "司機列表");
            seedPermission("FMS", "FMS_DRIVER_VIEW", "司機列表V", "");
            seedPermission("FMS", "FMS_DRIVER_CREATE", "司機列表C", "");
            seedPermission("FMS", "FMS_DRIVER_UPDATE", "司機列表U", "");
            seedPermission("FMS", "FMS_DRIVER_DELETE", "司機列表D", "");
            seedPermission("TOM", "TOM_ORDER", "訂單資料", "訂單資料");
            seedPermission("TOM", "TOM_ORDER_VIEW", "訂單資料V", "");
            seedPermission("TOM", "TOM_ORDER_CREATE", "訂單資料C", "");
            seedPermission("TOM", "TOM_ORDER_UPDATE", "訂單資料U", "");
            seedPermission("TOM", "TOM_ORDER_DELETE", "訂單資料D", "");
            seedPermission("TOM", "TOM_ORDER_ASSIGN", "訂單資料A", "");
            seedPermission("TOM", "TOM_ORDER_STATUS", "訂單資料S", "");

            // 2) Roles
            seedRole("SYS_ADMIN", "系統管理員", "全開、最高權限");
            seedRole("COMPANY_ADMIN", "公司管理員", "UPMS + FMS + TOM（管理公司自己資料）");
            seedRole("DISPATCH", "OP 調度員", "FMS");
            seedRole("DRIVER", "司機", "FMS（僅可操作自己的工作）");
            seedRole("CUSTOMER", "顧客 / 託運客戶", "");
            seedRole("BACK_OFFICE", "內勤人員", "單據、對帳、報表");
            seedRole("STAFF", "行政人員", "人員、車輛基本資料維護");
            seedRole("USER", "一般使用者", "");

            // 3) Users (with BCrypt)
            seedUser("admin");
            seedUser("sys001");
            seedUser("sys002");
            seedUser("sys003");
            seedUser("ca001");
            seedUser("ca002");
            seedUser("ca003");
            seedUser("op001");
            seedUser("op002");
            seedUser("op003");
            seedUser("dd001");
            seedUser("dd002");
            seedUser("dd003");
            seedUser("userA");
            seedUser("userB");
            seedUser("userC");

            initAdmin();
            log.info("[UPMS] 種子資料初始化完成！");
        };
    }

    private void seedPermission(String systemCode, String code, String name, String desc) {
        if (permissionService.existsByCode(code)) {
            log.info("权限已存在：{}", code);
            return;
        }
        var req = new UpmsPermissionCreateReq();
        req.setSystemCode(systemCode);
        req.setCode(code);
        req.setName(name);
        req.setDescription(desc);
        permissionService.create(req);
        log.info("建立 Permission：{}", code);
    }

    private void seedRole(String code, String name, String desc) {
        if (roleService.existsByCode(code)) {
            log.info("角色已存在：{}", code);
            return;
        }
        var req = new UpmsRoleCreateReq();
        req.setCode(code);
        req.setName(name);
        req.setDescription(desc);
        roleService.create(req);
        log.info("建立 Role：{}", code);
    }

    private void seedUser(String username) {
        if (userService.existsByUsername(username)) {
            log.info("使用者已存在：{}", username);
            return;
        }
        var req = new UpmsUserCreateReq();
        req.setUsername(username);
        req.setPassword("truck123456");
        userService.create(req);
        log.info("建立使用者：{}", username);
    }

    private void initAdmin() {
        // 0) 固定代碼（避免魔法字串散落）
        final String ADMIN_USERNAME = "admin";
        final String SYS_ADMIN_CODE = "SYS_ADMIN";

        // 1) 取出所有 permission codes（不分頁）
        List<String> permCodes = permissionService
                .pageForList(null, Pageable.unpaged())
                .getContent()
                .stream()
                .map(UpmsPermissionListResp::getCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();

        if (permCodes.isEmpty()) {
            log.warn("[initAdmin] 尚未建立任何 Permission，無法初始化 {} 權限", SYS_ADMIN_CODE);
            return;
        }

        // 2) 找 SYS_ADMIN 角色（找不到會丟 BusinessException；這裡不做 role==null）
        UpmsRoleResp role = roleService.findByCode(SYS_ADMIN_CODE);

        // 3) 找 admin 使用者（找不到會丟 BusinessException；這裡不做 user==null）
        UpmsUserResp user = userService.findByUsername(ADMIN_USERNAME);

        // 4) 指派角色給 admin（建議 assignRole() 內部做「已存在就略過」以利 seed 重跑）
        userRoleService.assignRole(user.getUuid(), role.getCode());
        log.info("[initAdmin] 已確保使用者 {} 擁有角色 {}", user.getUsername(), role.getCode());

        // 5) 把所有 permission 覆蓋式綁到 SYS_ADMIN（一次 replace，不要逐筆 assign）
        roleService.replacePermissions(role.getUuid(), permCodes);

        log.info("[initAdmin] 已將 {} 個 Permission 綁到角色 {}", permCodes.size(), role.getCode());
    }
}
