package com.xk.base.web;

import org.slf4j.MDC;

public final class Trace {
    private static final String KEY = "traceId";

    public static String currentId() {
        return MDC.get(KEY);
    }

    private Trace() {
    }
}
