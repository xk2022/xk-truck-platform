package com.xk.truck.web;

import com.xk.base.exception.ResourceNotFoundException;
import com.xk.base.web.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.xk.truck.api")
public class TruckExceptionHandler {

    /**
     * 專案內找不到資源 → 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResult<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResult.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage() != null ? ex.getMessage() : "找不到指定資源")
                .errorDetails(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 其他未預期錯誤 → 500
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleOther(Exception ex) {
        return ApiResult.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("系統忙線中，請稍後再試")
                .errorDetails(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
