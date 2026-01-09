package com.xk.truck.ordercore.application.port.out;

import com.xk.truck.ordercore.domain.model.OrderCore;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Outbound Port: OrderCoreRepository
 * Layer        : Application (Port Out)
 * Purpose      : 定義 OrderCore Aggregate 的持久化需求（Aggregate Repository）
 * ===============================================================
 * <p>
 * Design Notes:
 * - 不暴露 JPA / Entity / DB 細節
 * - 實作由 infra adapter（例如 OrderCoreRepositoryJpaAdapter）負責
 * - existsByOrderNo 僅供 fast-fail / UX 輔助，唯一性最終由 DB unique constraint 保證
 */
public interface OrderCoreRepository {

    /**
     * 儲存 OrderCore Aggregate（create / update 皆可）
     */
    OrderCore save(OrderCore aggregate);

    /**
     * 依 orderUuid 還原 Aggregate
     */
    Optional<OrderCore> findByOrderUuid(UUID orderUuid);

    /**
     * 檢查訂單編號是否存在（fast-fail / UX）
     */
    boolean existsByOrderNo(String orderNo);
}
