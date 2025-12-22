package com.xk.truck.tom.application.usecase.cmd;

import com.xk.truck.tom.domain.model.OrderStatus;
import com.xk.truck.tom.domain.model.OrderType;

import lombok.Data;

import java.util.UUID;

@Data
public class FindOrderCmd {

    private OrderType orderType;
    private OrderStatus status;
    private UUID customerUuid;
    private String keyword;
}
