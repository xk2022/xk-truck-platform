package com.xk.truck.tom.infra.persistence.repository;

import com.xk.base.exception.BusinessException;
import com.xk.truck.tom.application.mapper.ImportOrderMapper;
import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.domain.repository.OrderRepository;
import com.xk.truck.tom.infra.persistence.entity.OrderEntity;
import com.xk.truck.tom.infra.persistence.jpa.JpaOrderRepository;
import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpa;
    private final ImportOrderMapper importOrderMapper;
    private final OrderMapper orderMapper;

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return jpa.existsByOrderNo(orderNo);
    }

    @Override
    public Order save(Order aggregate) {
        OrderEntity entity = importOrderMapper.toEntity(aggregate);

        // 自動寫第一筆 CREATED status log（你的 entity 有 addStatusLog）
        entity.addStatusLog(null, aggregate.getOrderStatus(), "created");

        OrderEntity saved = jpa.save(entity);

        return importOrderMapper.toDomain(saved);
    }

    @Override
    public Page<OrderListItemProjection> pageForListProjection(FindOrderQry q, Pageable pageable) {
        return jpa.pageForListProjection(q, pageable);
    }

    @Override
    public Order getOrderDetail(UUID id) {
        OrderEntity entity = jpa.findDetailByUuid(id)
                .orElseThrow(() -> new BusinessException("TOM_ORDER_NOT_FOUND", "查無訂單：" + id));

        return importOrderMapper.toDomain(entity);
    }
}
