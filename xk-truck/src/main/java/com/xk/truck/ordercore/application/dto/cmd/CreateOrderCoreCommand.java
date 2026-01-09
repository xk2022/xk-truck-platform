package com.xk.truck.ordercore.application.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * ===============================================================
 * Application Command: CreateOrderCoreCommand
 * Layer               : Application (Use Case Input)
 * Purpose             : 表達「建立 order-core 訂單」的使用者意圖
 * ===============================================================
 *
 * Design Notes:
 * - 為系統內部語言（不含 HTTP / JSON / DB 語意）
 * - 僅包含 order-core 建立所需的最小資料
 * - orderNo 必須由呼叫端（Orchestrator）事先決定
 *
 * Validation:
 * - @NotBlank 用於保護 Use Case 不變式
 * - 非 Controller 驗證用途
 */
@Value
public class CreateOrderCoreCommand {

    /**
     * 全系統唯一訂單編號
     * - 由外部流程（如 TOM Orchestrator）產生
     * - order-core 不負責產生或重試
     */
    @NotBlank
    String orderNo;
}
