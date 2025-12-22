// domain/service/OrderDomainService.java
package com.xk.truck.tom.domain.service;

import com.xk.truck.tom.application.usecase.cmd.FindOrderCmd;
import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.domain.model.CreateImportOrderSpec;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.domain.model.OrderListItem;
import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * ===============================================================
 * UseCase Interface : CreateImportOrderUseCase
 * Layer             : Application
 * Purpose           : 建立進口訂單（IMPORT）
 * ===============================================================
 */
public interface OrderDomainService {

    Order createImport(CreateImportOrderSpec spec);

    Page<OrderListItemProjection> pageForListProjection(FindOrderQry q, Pageable pageable);

    Order getOrderDetail(UUID id);
}
