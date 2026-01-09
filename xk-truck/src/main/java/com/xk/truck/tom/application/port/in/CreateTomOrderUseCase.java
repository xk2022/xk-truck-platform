package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.CreateTomOrderCommand;

/**
 * ===============================================================
 * Inbound Port: CreateTomOrderUseCase
 * Layer       : Application (Use Case)
 * Purpose     : 建立 TOM 運輸訂單（流程層）的用例入口
 * ===============================================================
 * <p>
 * Responsibilities:
 * - 表達「建立 TOM 訂單」的使用者意圖（Use Case Contract）
 * - 由實作類負責交易邊界與流程協調（Orchestration）
 * <p>
 * Non-Responsibilities:
 * - 不處理 HTTP DTO mapping（Controller/ApiMapper）
 * - 不處理 Persistence / JPA Entity（Infra Adapter）
 * - 不承載領域規則本體（Domain Aggregate / Domain Policy）
 * <p>
 * Notes:
 * - 建議所有 UseCase 介面統一使用 execute(cmd) 作為單一入口
 * - 新功能（如 Assign / Cancel）新增新的 UseCase 介面即可
 * ===============================================================
 */
public interface CreateTomOrderUseCase {

    TomOrderResult execute(CreateTomOrderCommand cmd);
}
