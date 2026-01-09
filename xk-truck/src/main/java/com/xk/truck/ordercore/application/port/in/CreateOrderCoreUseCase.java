package com.xk.truck.ordercore.application.port.in;

import com.xk.truck.ordercore.application.dto.OrderCoreResult;
import com.xk.truck.ordercore.application.dto.cmd.CreateOrderCoreCommand;

/**
 * ===============================================================
 * Inbound Port: CreateOrderCoreUseCase
 * Layer       : Application (Use Case)
 * Purpose:
 * - 處理「建立 order-core 訂單」這個單一使用者意圖
 * - 建立後訂單狀態一律為 {@code OrderStatus.OPEN}
 * ===============================================================
 * Design Notes:
 * - 本介面代表一條完整的 Application Use Case
 * - 僅定義「要做什麼」，不包含任何技術細節
 * - 交易邊界、錯誤處理、重試策略由實作層負責
 * <p>
 * Naming Convention:
 * - 使用 {@code execute} 作為統一入口點
 * - 每個 UseCase 僅暴露一個方法，避免意圖混雜
 * <p>
 * Non-Responsibilities:
 * - 不處理 HTTP / Controller 相關邏輯
 * - 不涉及 JPA / Repository 實作
 * - 不包含跨 Domain 的流程協調（由 Orchestrator 處理）
 * ===============================================================
 */
public interface CreateOrderCoreUseCase {

    /**
     * Execute the use case.
     *
     * @param cmd 建立訂單所需的最小指令資料（Application Command）
     * @return 建立完成的 order-core 結果（UUID / OrderNo / OrderStatus）
     * @throws com.xk.base.exception.BusinessException 若違反業務規則（例如訂單號已存在）
     */
    OrderCoreResult execute(CreateOrderCoreCommand cmd);
}
