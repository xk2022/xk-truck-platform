package com.xk.base.domain.jpa.spec;

import org.springframework.data.jpa.domain.Specification;

/**
 * ===============================================================
 * Spec Helper: EnabledSpec
 * ---------------------------------------------------------------
 * Purpose:
 * - 提供「enabled 狀態」的通用 Specification
 *
 * Design:
 * - enabled == null → 回傳 null（代表不套用條件）
 * - 預設欄位名為 "enabled"
 * - 可自訂欄位名（例如 "active", "isEnabled"）
 *
 * Usage:
 *   spec = SpecUtils.and(spec, EnabledSpec.eq(query.getEnabled()));
 *   spec = SpecUtils.and(spec, EnabledSpec.eq(query.getEnabled(), "enabled"));
 * ===============================================================
 */
public final class EnabledSpec {

    private EnabledSpec() {}

    /**
     * enabled = {true/false}（欄位名預設 enabled）
     */
    public static <T> Specification<T> eq(Boolean enabled) {
        return eq(enabled, "enabled");
    }

    /**
     * enabled = {true/false}（可自訂欄位名）
     */
    public static <T> Specification<T> eq(Boolean enabled, String field) {
        if (enabled == null) {
            return null;
        }

        final String f = (field == null || field.isBlank()) ? "enabled" : field;

        return (root, query, cb) -> cb.equal(root.get(f), enabled);
    }

    /**
     * enabled = true（欄位名預設 enabled）
     */
    public static <T> Specification<T> isTrue() {
        return isTrue("enabled");
    }

    /**
     * enabled = true（可自訂欄位名）
     */
    public static <T> Specification<T> isTrue(String field) {
        return eq(Boolean.TRUE, field);
    }

    /**
     * enabled = false（欄位名預設 enabled）
     */
    public static <T> Specification<T> isFalse() {
        return isFalse("enabled");
    }

    /**
     * enabled = false（可自訂欄位名）
     */
    public static <T> Specification<T> isFalse(String field) {
        return eq(Boolean.FALSE, field);
    }
}
