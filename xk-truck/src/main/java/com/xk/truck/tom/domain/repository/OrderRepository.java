package com.xk.truck.tom.domain.repository;

import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.dto.OrderListItemResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface OrderRepository {

    boolean existsByOrderNo(String orderNo);

    Order save(Order aggregate);

    Page<OrderListItemProjection> pageForListProjection(FindOrderQry q, Pageable pageable);

    Order getOrderDetail(UUID id);
}
