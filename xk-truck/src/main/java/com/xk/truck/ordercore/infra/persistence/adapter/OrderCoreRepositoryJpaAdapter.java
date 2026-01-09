package com.xk.truck.ordercore.infra.persistence.adapter;

import com.xk.truck.ordercore.application.port.out.OrderCoreRepository;
import com.xk.truck.ordercore.domain.model.OrderCore;
import com.xk.truck.ordercore.infra.persistence.entity.OrderCoreEntity;
import com.xk.truck.ordercore.infra.persistence.jpa.JpaOrderCoreRepository;
import com.xk.truck.ordercore.infra.persistence.mapper.OrderCorePersistenceMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Adapter: OrderCoreRepositoryJpaAdapter
 * Layer   : Infrastructure / Persistence
 * Purpose :
 * - 實作 Domain Repository（OrderCoreRepository）
 * - 使用 JPA 作為持久化機制
 * ===============================================================
 */
@Repository
@RequiredArgsConstructor
public class OrderCoreRepositoryJpaAdapter implements OrderCoreRepository {

    private final JpaOrderCoreRepository jpaRepository;

    @Override
    @Transactional
    public OrderCore save(OrderCore aggregate) {
        OrderCoreEntity entity = OrderCorePersistenceMapper.toEntity(aggregate);
        OrderCoreEntity saved = jpaRepository.save(entity);
        return OrderCorePersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderCore> findByOrderUuid(UUID orderUuid) {
        return jpaRepository.findById(orderUuid)
                .map(OrderCorePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderNo(String orderNo) {
        return jpaRepository.existsByOrderNo(orderNo);
    }
}
