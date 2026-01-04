package com.xk.base.domain.jpa.spec;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 * ===============================================================
 * Spec Helper: KeywordSpec
 * ---------------------------------------------------------------
 * Purpose:
 * - 提供「關鍵字 keyword」的通用 Specification 組裝工具
 * - 常見用法：code/name/description LIKE %keyword%（不分大小寫）
 * <p>
 * Design:
 * - keyword 为空 → 回傳 null（方便 spec.and(...) 直接串）
 * - 欄位名由呼叫端傳入（避免 base 綁死特定 entity）
 * - 預設不分大小寫：lower(field) LIKE lower(%keyword%)
 * <p>
 * Notes:
 * - 若你希望支援 escape（避免 %/_ 被當 wildcard），可改用 escapeLike(...) 方法
 * 並在 cb.like(..., ..., '\\') 指定 escape char。
 * ===============================================================
 */
public final class KeywordSpec {

    private KeywordSpec() {
    }

    /**
     * 在多個欄位中以 OR 方式做 keyword LIKE（大小寫不敏感）
     * <p>
     * 例：
     * spec.and(KeywordSpec.likeAny(query.getKeyword(), "code", "name"))
     */
    public static <T> Specification<T> likeAny(String keyword, String... fields) {
        if (!StringUtils.hasText(keyword) || fields == null || fields.length == 0) {
            return null;
        }

        final String kw = "%" + normalize(keyword) + "%";

        return (root, query, cb) -> {
            var predicates = new jakarta.persistence.criteria.Predicate[fields.length];

            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];

                // root.get(field) 可能是 String 或其他型別，這裡強制轉成 String expression 後 lower
                Expression<String> expr = toLowerString(root, cb, field);

                predicates[i] = cb.like(expr, kw);
            }

            return cb.or(predicates);
        };
    }

    /**
     * 常見快捷：code OR name LIKE %keyword%（大小寫不敏感）
     */
    public static <T> Specification<T> codeOrName(String keyword) {
        return likeAny(keyword, "code", "name");
    }

    /**
     * 常見快捷：code OR name OR description LIKE %keyword%（大小寫不敏感）
     */
    public static <T> Specification<T> codeNameOrDescription(String keyword) {
        return likeAny(keyword, "code", "name", "description");
    }

    // ------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------

    private static String normalize(String s) {
        return s.trim().toLowerCase();
    }

    private static <T> Expression<String> toLowerString(Root<T> root, CriteriaBuilder cb, String field) {
        // cb.lower 需要 Expression<String>，這裡把任意欄位轉成 String 後 lower
        // 若欄位本來就是 String，as(String.class) 仍可正常運作
        return cb.lower(root.get(field).as(String.class));
    }

    // ------------------------------------------------------------
    // Optional: escape LIKE wildcards (if you need)
    // ------------------------------------------------------------
    // private static String escapeLike(String s) {
    //     // Escape: \, %, _
    //     return s.replace("\\", "\\\\")
    //             .replace("%", "\\%")
    //             .replace("_", "\\_");
    // }
}
