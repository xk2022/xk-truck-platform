package com.xk.truck.order.domain.repo;

import com.xk.truck.order.domain.model.Order;
import com.xk.truck.order.domain.model.OrderStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    Page<Order> findByStatusAndCreatedTimeBetween(
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
