package com.xk.truck.tom.infra.persistence.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.tom.domain.model.TomOrder;
import com.xk.truck.tom.infra.persistence.entity.TomOrderEntity;

/**
 * ===============================================================
 * Mapper: OrderCorePersistenceMapper
 * Layer : Infrastructure / Persistence
 * Purpose:
 * - OrderCore (Domain Aggregate) <-> OrderCoreEntity (JPA Entity)
 * ===============================================================
 */
public final class TomOrderPersistenceMapper {

    public static TomOrderEntity toEntity(TomOrder aggregate) {
        return XkBeanUtils.copyProperties(aggregate, TomOrderEntity::new);
    }

    public static TomOrder toDomain(TomOrderEntity entity) {
        if (entity == null) return null;

        // 注意：此處假設 OrderCore 提供 rehydrate factory
        return TomOrder.rehydrate(
                entity.getOrderUuid(),
                entity.getOrderNo(),
                entity.getOrderType(),
                entity.getTomStatus(),
                entity.getCustomerUuid(),
                entity.getCustomerName(),
                entity.getPickupAddress(),
                entity.getDeliveryAddress(),
                entity.getScheduledAt(),
                entity.getCustomerRefNo(),
                entity.getRemark()
        );
    }
}
