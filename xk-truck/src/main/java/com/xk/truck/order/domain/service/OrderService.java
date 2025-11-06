package com.xk.truck.order.domain.service;

import com.xk.truck.order.controller.api.dto.OrderCreateReq;
import com.xk.truck.order.controller.api.dto.OrderResp;
import com.xk.truck.order.domain.model.Order;
import com.xk.truck.order.domain.model.OrderStatus;
import com.xk.truck.order.domain.repo.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repo;

    // 允許的狀態轉換
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED.put(OrderStatus.CREATED,   Set.of(OrderStatus.ACCEPTED, OrderStatus.CANCELED));
        ALLOWED.put(OrderStatus.ACCEPTED,  Set.of(OrderStatus.DISPATCHED, OrderStatus.CANCELED));
        ALLOWED.put(OrderStatus.DISPATCHED,Set.of(OrderStatus.PICKED_UP, OrderStatus.CANCELED));
        ALLOWED.put(OrderStatus.PICKED_UP, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELED));
        ALLOWED.put(OrderStatus.DELIVERED, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELED));
        ALLOWED.put(OrderStatus.COMPLETED, Set.of()); // 終態
        ALLOWED.put(OrderStatus.CANCELED,  Set.of()); // 終態
    }

    @Transactional
    public OrderResp create(OrderCreateReq req) {
        if (repo.existsByOrderNo(req.getOrderNo())) {
            throw new IllegalArgumentException("訂單編號已存在: " + req.getOrderNo());
        }

        Order o = Order.builder()
                .orderNo(req.getOrderNo())
                .customerName(req.getCustomerName())
                .pickupAddress(req.getPickupAddress())
                .deliveryAddress(req.getDeliveryAddress())
                .scheduledAt(req.getScheduledAt())
                .status(OrderStatus.CREATED)
                .build();

        o = repo.save(o);
        return map(o);
    }

    @Transactional(readOnly = true)
    public Page<OrderResp> page(int page, int size, Sort sort) {
        Page<Order> p = repo.findAll(PageRequest.of(page, size, sort));
        return p.map(this::map);
    }

    @Transactional(readOnly = true)
    public OrderResp get(String uuid) {
        return map(repo.findById(Long.valueOf(uuid))
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + uuid)));
    }

    @Transactional
    public OrderResp changeStatus(String uuid, OrderStatus to) {
        Order o = repo.findById(Long.valueOf(uuid))
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + uuid));

        var from = o.getStatus();
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException("狀態不可由 " + from + " 轉為 " + to);
        }
        o.setStatus(to);
        return map(o);
    }

    private OrderResp map(Order o) {
        return new OrderResp(
                o.getUuid(),
                o.getOrderNo(),
                o.getCustomerName(),
                o.getPickupAddress(),
                o.getDeliveryAddress(),
                o.getScheduledAt(),
                o.getStatus(),
                o.getCreatedTime(),
                o.getUpdatedTime()
        );
    }
}
