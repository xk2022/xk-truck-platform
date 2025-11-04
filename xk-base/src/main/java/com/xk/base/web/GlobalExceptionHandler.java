package com.xk.base.web;

import com.xk.base.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - 參數驗證錯誤
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<?> handleValidation(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a, b) -> a));
        return ApiResult.failure(HttpStatus.BAD_REQUEST, "參數驗證失敗", details);
    }

    // 403 - 方法層（@PreAuthorize）授權拒絕
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<?> handleAuthzDenied(AuthorizationDeniedException ex, HttpServletRequest req) {
        log.warn("403 Forbidden (method) @ {} - {}", req.getRequestURI(), ex.getMessage());
        return ApiResult.failure(HttpStatus.FORBIDDEN, "沒有存取此資源的權限", Map.of("reason", "Access Denied"));
    }

    // 403 - URL / Filter 層授權拒絕
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<?> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("403 Forbidden (filter) @ {} - {}", req.getRequestURI(), ex.getMessage());
        return ApiResult.failure(HttpStatus.FORBIDDEN, "沒有存取此資源的權限", Map.of("reason", "Access Denied"));
    }

    // 409 - 業務衝突（用 ResponseEntity 明確控制狀態碼）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<?>> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("409 BusinessException [{}] @ {} - {}", ex.getCode(), req.getRequestURI(), ex.getMessage());
        var body = ApiResult.failure(HttpStatus.CONFLICT, ex.getMessage(), ex.getDetails());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 500 - 兜底處理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("500 Unhandled @ {} - {}", req.getRequestURI(), ex.getMessage(), ex);
        return ApiResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, "系統忙線中，請稍後再試", null);
    }
}
