package com.xk.truck.tom.application.dto.cmd;

import lombok.Data;

import java.util.UUID;

@Data
public class AcceptTomOrderCommand {
    private UUID orderUuid;
}
