package com.xk.base.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 通用的 API 回應格式（支援 Builder 模式與 Swagger 文件）
 *
 * @param <T> 回傳資料的型別
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApiResult<T> {

    /**
     * 狀態碼，例如：200（成功），400（用戶錯誤），500（伺服器錯誤）
     */
    private int code;

    /**
     * 回應消息，可用於簡要描述操作結果
     */
    private String message;

    /**
     * 回應的數據內容
     */
    private T data;

    /**
     * 錯誤詳情，用於返回詳細的錯誤信息（可選）
     */
    private Object errorDetails;

    /**
     * 回應時間戳
     */
    private LocalDateTime timestamp = LocalDateTime.now();


    // ---------- 靜態工廠方法 ----------

    /**
     * 快速構造成功的回應
     *
     * @param <T>  數據類型
     * @param data 回應數據
     * @return 成功的 BaseResult 實例
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(HttpStatus.OK.value(), null, data, null, LocalDateTime.now());
    }
    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<>(HttpStatus.OK.value(), message, data, null, LocalDateTime.now());
    }

    /**
     * 快速構造失敗的回應
     *
     * @param status       錯誤碼 HttpStatus
     * @param message      錯誤消息
     * @param errorDetails 錯誤詳情（可為 null）
     * @param <T>          數據類型
     * @return 失敗的 BaseResult 實例
     */
    public static <T> ApiResult<T> failure(HttpStatus status, String message, Object errorDetails) {
        return new ApiResult<>(status.value(), message, null, errorDetails, LocalDateTime.now());
    }

    // ---------- 狀態鏈式設定 ----------
    public ApiResult<T> withStatus(int status) {
        this.code = status;
        return this;
    }

}
