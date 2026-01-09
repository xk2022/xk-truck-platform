package com.xk.truck.ordercore.infra.persistence.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.ordercore.domain.model.OrderCore;
import com.xk.truck.ordercore.infra.persistence.entity.OrderCoreEntity;

/**
 * ===============================================================
 * Mapper: OrderCorePersistenceMapper
 * Layer : Infrastructure / Persistence
 * Purpose:
 * - OrderCore (Domain Aggregate) <-> OrderCoreEntity (JPA Entity)
 * <p>
 * Design Notes:
 * - 僅供 infra adapter 使用
 * - 明確屬於 Persistence 層，而非 Domain
 * - 使用 XkBeanUtils 作為技術性 mapping 工具
 * <p>
 * Important:
 * - 不可被 Domain / Application 層依賴
 * - Domain Aggregate 應透過 rehydrate / factory 建立一致性
 * ===============================================================
 */
public final class OrderCorePersistenceMapper {

    public static OrderCoreEntity toEntity(OrderCore aggregate) {
        return XkBeanUtils.copyProperties(aggregate, OrderCoreEntity::new);
    }

    public static OrderCore toDomain(OrderCoreEntity entity) {
        if (entity == null) return null;

        // 注意：此處假設 OrderCore 提供 rehydrate factory
        return OrderCore.rehydrate(
                entity.getOrderUuid(),
                entity.getOrderNo(),
                entity.getOrderCoreStatus(),
                entity.getCreatedTime(),
                entity.getCancelledAt(),
                entity.getClosedAt()
        );
    }
}
