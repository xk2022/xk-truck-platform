package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.cmd.CancelTomOrderCommand;

public interface CancelTomOrderUseCase {
    void cancel(CancelTomOrderCommand cmd);
}
