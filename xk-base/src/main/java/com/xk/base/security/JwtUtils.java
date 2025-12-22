package com.xk.base.security;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT 相關工具方法
 *
 * 純工具類，不進 Spring IOC
 */
public final class JwtUtils {

    // 防止被 new
    private JwtUtils() {
    }

    /**
     * 將各種可能型別（List<?> / String[] / String / null）安全轉為 String[]
     */
    public static String[] normalizeRoles(Object rolesObj) {
        if (rolesObj == null) {
            return new String[0];
        }

        // 單一字串
        if (rolesObj instanceof String s) {
            s = s.trim();
            return s.isEmpty() ? new String[0] : new String[]{s};
        }

        // 已經是陣列
        if (rolesObj instanceof String[] arr) {
            return arr;
        }

        // List 轉陣列
        if (rolesObj instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    String v = o.toString().trim();
                    if (!v.isEmpty()) {
                        out.add(v);
                    }
                }
            }
            return out.toArray(String[]::new);
        }

        // 其他未知型別 → 保底處理
        String value = rolesObj.toString().trim();
        return value.isEmpty() ? new String[0] : new String[]{value};
    }
}
