package com.xk.truck.tom.application.usecase;

import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import java.util.UUID;

/**
 * ===============================================================
 * UseCase Class : GetOrderDetailUseCase
 * Layer         :
 * Purpose       :
 * Notes         :
 * ===============================================================
 */
public interface GetOrderDetailUseCase {

    OrderDetailResp findById(UUID id);
}
