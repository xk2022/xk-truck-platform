package com.xk.truck.tom.application.usecase;

import com.xk.truck.tom.application.usecase.cmd.CreateOrderReq;
import com.xk.truck.tom.application.usecase.dto.OrderResp;

public interface CreateOrderUseCase {

    OrderResp execute(CreateOrderReq req);
}
