package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.AssignTomOrderCommand;

public interface AssignTomOrderUseCase {
    TomOrderResult assign(AssignTomOrderCommand cmd);
}
