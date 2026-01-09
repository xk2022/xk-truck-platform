package com.xk.truck.tom.application.dto.qry;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindTomOrderQry {

//    private OrderType orderType;
//    private OrderStatus orderStatus;
    private UUID customerUuid;

    private String orderNoLike;
    private String containerNoLike;

    private ZonedDateTime createdFrom;
    private ZonedDateTime createdTo;
}
