package com.xk.truck.upms.controller.api.dto.profile;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserProfileResp {

    private UUID id;
    private String username;
    private Boolean enabled;
    private Boolean locked;

    private ProfileResp profile;

    private List<RoleResp> roles;

    private List<String> permissions;

    private List<LoginHistoryResp> loginHistory;

    @Data
    public static class ProfileResp {
        private String name;
        private String nickName;
        private String email;
        private String phone;
        private String avatarUrl;
    }

    @Data
    public static class RoleResp {
        private String code;
        private String name;
    }

    @Data
    public static class LoginHistoryResp {
        private LocalDateTime time;
        private String ip;
    }
}
