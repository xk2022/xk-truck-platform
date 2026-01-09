package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Schema(description = "建立使用者請求（Create User）")
@Data
public class UpmsUserCreateReq {

    @Schema(description = "帳號（唯一，用於登入）", example = "admin")
    @NotBlank(message = "帳號不可為空")
    private String username;

    @Schema(description = "初始密碼", example = "P@ssw0rd")
    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 20, message = "密碼長度需介於 8~20 字元")
    private String password;

    @Schema(description = "角色代碼清單（至少一個）", example = "[\"ADMIN\", \"UPMS_ADMIN\"]")
    @NotEmpty(message = "至少需指定一個角色")
    private Set<String> roleCodes;

    // ----------------------------------------------------------------
    // Optional Profile Fields（非必要，後續可補）
    // ----------------------------------------------------------------

    @Schema(description = "顯示名稱", example = "王小明")
    private String name;

    @Schema(description = "Email（選填）", example = "user@example.com")
    private String email;

    @Schema(description = "電話（選填）", example = "0912-345-678")
    private String phone;
}
