package com.xk.truck.tom.infra.persistence.jpa;

import com.xk.truck.tom.infra.persistence.entity.OrderStatusLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaOrderStatusLogRepository extends JpaRepository<OrderStatusLogEntity, UUID> {
}
