package com.xk.base.web.exception;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final String code;
    private final Map<String, Object> details;

    public BusinessException(String code, String message) {
        this(code, message, null);
    }

    public BusinessException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
