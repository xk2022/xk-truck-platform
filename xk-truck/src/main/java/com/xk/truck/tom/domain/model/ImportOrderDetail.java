package com.xk.truck.tom.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImportOrderDetail {
    private String deliveryOrderLocation;
    private String blNo;
    private String importDeclNo;
    private LocalDateTime customsReleaseTime;
    private String warehouse;
    private String arrivalNotice;
}
