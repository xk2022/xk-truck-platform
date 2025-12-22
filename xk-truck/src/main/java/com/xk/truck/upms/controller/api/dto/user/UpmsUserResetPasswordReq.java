package com.xk.truck.upms.controller.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpmsUserResetPasswordReq {

    @NotBlank(message = "新密碼不能為空")
    private String newPassword;

    // 你若要更嚴格：
    // @Size(min=8, max=72)
    // @Pattern(...)
}
