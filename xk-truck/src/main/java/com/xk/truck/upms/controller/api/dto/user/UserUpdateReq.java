package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Set;

@Schema(description = "更新使用者請求")
@Data
public class UserUpdateReq {
    private String username;
    private Set<String> roleCodes;
}
