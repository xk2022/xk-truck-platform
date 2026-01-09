package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.AcceptTomOrderCommand;

public interface AcceptTomOrderUseCase {
    TomOrderResult accept(AcceptTomOrderCommand cmd);
}
