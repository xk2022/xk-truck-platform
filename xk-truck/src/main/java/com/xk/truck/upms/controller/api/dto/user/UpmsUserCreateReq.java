package com.xk.truck.upms.controller.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Schema(description = "建立使用者請求（Create User）")
@Data
public class UpmsUserCreateReq {

    @Schema(description = "帳號（唯一，用於登入）", example = "admin")
    @NotBlank(message = "帳號不能為空")
    @Size(max = 80, message = "帳號長度不可超過 80")
    private String username;

    @Schema(description = "初始密碼（明碼，後端會進行 BCrypt）", example = "P@ssw0rd123")
    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, max = 20, message = "密碼長度需介於 8~20")
    private String password;

    @Schema(description = "角色代碼清單（至少一個）", example = "[\"ADMIN\",\"UPMS_ADMIN\"]")
    @NotEmpty(message = "至少需指定一個角色")
    private Set<@NotBlank(message = "角色代碼不可為空") String> roleCodes;

    @Schema(description = "顯示名稱（選填）", example = "王小明")
    @Size(max = 80, message = "顯示名稱長度不可超過 80")
    private String name;

    @Schema(description = "Email（選填）", example = "user@example.com")
    @Email(message = "Email 格式不正確")
    @Size(max = 120, message = "Email 長度不可超過 120")
    private String email;

    @Schema(description = "電話（選填）", example = "0912-345-678")
    @Size(max = 30, message = "電話長度不可超過 30")
    private String phone;
}
