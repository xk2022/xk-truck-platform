package com.xk.truck.ordercore.infra.persistence.jpa;

import com.xk.truck.ordercore.infra.persistence.entity.OrderCoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * ===============================================================
 * Repository: JpaOrderCoreRepository
 * Layer     : Infrastructure / Persistence (Spring Data JPA)
 * Purpose   : OrderCore Aggregate 的 JPA 存取
 * <p>
 * Notes:
 * - 僅供 infra adapter 使用
 * - 不可被 Application / Domain 直接依賴
 * - orderNo 為資料庫層唯一鍵
 * ===============================================================
 */
public interface JpaOrderCoreRepository extends JpaRepository<OrderCoreEntity, UUID> {

    boolean existsByOrderNo(String orderNo);
}
