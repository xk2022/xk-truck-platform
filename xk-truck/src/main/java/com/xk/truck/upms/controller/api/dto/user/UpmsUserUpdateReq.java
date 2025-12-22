package com.xk.truck.upms.controller.api.dto.user;

import com.xk.truck.upms.controller.api.dto.profile.UserProfileReq;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Set;

@Schema(description = "更新使用者請求")
@Data
public class UpmsUserUpdateReq {
    private String username;
    private Set<String> roleCodes;
    private UserProfileReq profile;
}
