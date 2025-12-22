// src/main/java/com/xk/truck/tom/application/dto/UpdateOrderStatusCmd.java
package com.xk.truck.tom.application.dto;

import com.xk.truck.tom.domain.model.OrderStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateOrderStatusCmd {
    private OrderStatus toStatus;
    private String note;
    private String operatorUuid; // 可接 UPMS user
}
