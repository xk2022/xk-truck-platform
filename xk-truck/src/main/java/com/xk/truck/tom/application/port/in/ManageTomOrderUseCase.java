package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.CreateTomOrderCommand;

/**
 * ===============================================================
 * Inbound Port : CreateOrderUseCase
 * Purpose      : 對外提供「建立 TOM 訂單」的能力
 * <p>
 * Design Notes:
 * - Controller / Batch / Message Consumer 都只能呼叫這個介面
 * - 不暴露 Entity / JPA / Domain 細節
 * ===============================================================
 */
public interface ManageTomOrderUseCase {

    TomOrderResult create(CreateTomOrderCommand cmd);

//    TomOrderResp assign(UUID orderId, AssignTomOrderReq req);
//    TomOrderResp dispatch(UUID orderId);
//    TomOrderResp complete(UUID orderId);
//    TomOrderResp cancel(UUID orderId);
}

