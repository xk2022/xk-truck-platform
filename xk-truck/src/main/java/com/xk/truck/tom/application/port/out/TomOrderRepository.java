package com.xk.truck.tom.application.port.out;

import com.xk.truck.tom.domain.model.TomOrder;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Outbound Port: TomOrderRepository
 * Layer        : Application (Port Out)
 * Purpose      : 定義 TomOrder Aggregate 的持久化需求（由 infra 實作）
 * ===============================================================
 */
public interface TomOrderRepository {

    TomOrder save(TomOrder aggregate);

    Optional<TomOrder> findByOrderUuid(UUID orderUuid);

    Optional<TomOrder> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);
}
