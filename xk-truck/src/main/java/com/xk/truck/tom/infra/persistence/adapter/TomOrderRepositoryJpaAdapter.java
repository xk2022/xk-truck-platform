package com.xk.truck.tom.infra.persistence.adapter;

import com.xk.truck.tom.domain.model.TomOrder;
import com.xk.truck.tom.application.port.out.TomOrderRepository;
import com.xk.truck.tom.infra.persistence.entity.TomOrderEntity;
import com.xk.truck.tom.infra.persistence.jpa.JpaTomOrderRepository;
import com.xk.truck.tom.infra.persistence.mapper.TomOrderPersistenceMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Adapter: TomOrderRepositoryJpaAdapter
 * Layer   : Infrastructure / Persistence
 * Purpose :
 * - 實作 Domain Repository（TomOrderRepository）
 * - 使用 JPA 作為持久化機制
 * ===============================================================
 */
@Repository
@RequiredArgsConstructor
public class TomOrderRepositoryJpaAdapter implements TomOrderRepository {

    private final JpaTomOrderRepository jpa;

    @Override
    @Transactional
    public TomOrder save(TomOrder aggregate) {
        TomOrderEntity entity = TomOrderPersistenceMapper.toEntity(aggregate);
        TomOrderEntity saved = jpa.save(entity);
        return TomOrderPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TomOrder> findByOrderUuid(UUID orderUuid) {
        return jpa.findById(orderUuid)
                .map(TomOrderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TomOrder> findByOrderNo(String orderNo) {
        return jpa.findByOrderNo(orderNo)
                .map(TomOrderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderNo(String orderNo) {
        return jpa.existsByOrderNo(orderNo);
    }
}
