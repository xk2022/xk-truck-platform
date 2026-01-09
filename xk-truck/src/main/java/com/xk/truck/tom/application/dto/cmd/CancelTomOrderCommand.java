package com.xk.truck.tom.application.dto.cmd;

import lombok.Data;

import java.util.UUID;

@Data
public class CancelTomOrderCommand {
    private UUID orderUuid;
    private String reason; // optional
}
