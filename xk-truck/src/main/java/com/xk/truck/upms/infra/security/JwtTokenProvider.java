package com.xk.truck.upms.infra.security;

import java.util.Set;
import java.util.UUID;

/**
 * JWT 產生器介面
 *
 * 實作可以放在 xk-base 或 upms.infra.security 中，
 * 這裡只定義 AuthService 會用到的方法。
 */
public interface JwtTokenProvider {

    /**
     * 產生 Access Token
     *
     * @param userId           使用者 UUID
     * @param username         帳號
     * @param roleCodes        角色代碼集合
     * @param permissionCodes  權限代碼集合
     * @return JWT 字串
     */
    String generateToken(UUID userId,
                         String username,
                         Set<String> roleCodes,
                         Set<String> permissionCodes);
}
