package com.xk.truck.upms.controller.api.dto.user;

import com.xk.truck.upms.controller.api.dto.profile.UserProfileCreateReq;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Schema(description = "建立使用者請求")
@Data
public class UpmsUserCreateReq {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Set<String> roleCodes;

}
