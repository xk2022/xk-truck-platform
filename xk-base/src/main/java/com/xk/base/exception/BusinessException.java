package com.xk.base.exception;

import lombok.Getter;

/**
 * 業務邏輯層（Service）專用例外。
 * 用於代表「已知邏輯錯誤」，例如資料重複、權限不足、條件不符等。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final Object details;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public BusinessException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.details = null;
    }
}
