package com.xk.truck.tom.application.usecase;

import com.xk.truck.tom.application.usecase.cmd.CreateImportOrderCmd;
import com.xk.truck.tom.application.usecase.dto.OrderResp;

/**
 * ===============================================================
 * UseCase Interface : CreateImportOrderUseCase
 * Layer             : Application
 * Purpose           : 建立進口訂單（IMPORT）
 * ===============================================================
 */
public interface CreateImportOrderUseCase {

    OrderResp execute(CreateImportOrderCmd cmd);
}
