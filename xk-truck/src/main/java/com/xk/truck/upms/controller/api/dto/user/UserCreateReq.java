package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Set;

@Schema(description = "建立使用者請求")
@Data
public class UserCreateReq {
    private String username;
    private String password;
    private Set<String> roleCodes;
}
