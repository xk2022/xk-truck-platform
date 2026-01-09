package com.xk.truck.tom.infra.persistence.jpa;

import com.xk.truck.tom.infra.persistence.entity.TomOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: JpaTomOrderRepository
 * Layer    : Infrastructure / Persistence (Spring Data JPA)
 * Purpose  : TomOrderEntity CRUD 與常用查詢
 * ===============================================================
 * Notes:
 * - 主鍵為 orderUuid（與 order-core 共享）
 * - orderNo 為唯一識別（對外查詢常用）
 */
public interface JpaTomOrderRepository extends JpaRepository<TomOrderEntity, UUID> {

    boolean existsByOrderNo(String orderNo);

    Optional<TomOrderEntity> findByOrderNo(String orderNo);

    // 主鍵就是 orderUuid，所以 findById 即可
    // Optional<TomOrderEntity> findById(UUID orderUuid);
}
