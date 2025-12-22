// src/main/java/com/xk/truck/tom/application/dto/CreateOrderCmd.java
package com.xk.truck.tom.application.dto;

import com.xk.truck.tom.domain.model.OrderType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateOrderCmd {
    private OrderType orderType;

    private String orderNo;          // 可由 Service 產生；若前端給也可驗重
    private String customerId;
    private String customerName;

    private String shippingCompany;
    private String vesselVoyage;

    private String containerNo;
    private String containerType;

    private String pol;
    private String pod;

    private String note;

    // detail（依 type）
    private CreateImportDetail importDetail;
    private CreateExportDetail exportDetail;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateImportDetail {
        private String deliveryOrderLocation;
        private String blNo;
        private String importDeclNo;
        private String warehouse;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateExportDetail {
        private String bookingNo;
        private String exportDeclNo;
        private String stuffingLocation;
    }
}
