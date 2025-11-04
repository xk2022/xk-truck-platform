package com.xk.base.web;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        String traceId,          // log 追蹤ID（可選）
        String code,             // 錯誤代碼，例如 "VALIDATION_ERROR"
        String message,          // 對前端友善的訊息
        Integer status,          // HTTP status
        String path,             // 請求路徑
        OffsetDateTime timestamp,// 伺服器時間
        Map<String, Object> details // 例如欄位錯誤
) {
}
