package com.xk.truck.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 用來綁定 application.yml 裡的 upms.seed.* 配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "upms.seed")
public class UpmsSeedProps {

    /**
     * 預設角色
     */
    private List<Role> roles;

    /**
     * 預設使用者
     */
    private List<User> users;

    @Getter
    @Setter
    public static class Role {
        private String code;   // 角色代碼，例如 "ADMIN"
        private String name;   // 顯示名稱，例如 "系統管理員"
    }

    @Getter
    @Setter
    public static class User {
        private String username;   // 帳號
        private String password;   // 密碼
        private List<String> roles; // 擁有的角色代碼
    }
}
