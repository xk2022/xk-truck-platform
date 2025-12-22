// src/main/java/com/xk/truck/tom/application/dto/AssignOrderCmd.java
package com.xk.truck.tom.application.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AssignOrderCmd {
    private String vehicleUuid;
    private String driverUuid;
    private String note;
}
