package com.xk.base.domain.jpa.spec;

import org.springframework.data.jpa.domain.Specification;

public final class SpecUtils {

    private SpecUtils() {}

    public static <T> Specification<T> and(
            Specification<T> base,
            Specification<T> addition
    ) {
        if (addition == null) return base;
        return base == null ? addition : base.and(addition);
    }

    public static <T> Specification<T> or(
            Specification<T> base,
            Specification<T> addition
    ) {
        if (addition == null) return base;
        return base == null ? addition : base.or(addition);
    }
}
