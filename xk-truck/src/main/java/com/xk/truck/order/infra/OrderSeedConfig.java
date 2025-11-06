package com.xk.truck.order.infra;

import com.xk.truck.order.domain.model.Order;
import com.xk.truck.order.domain.model.OrderStatus;
import com.xk.truck.order.domain.repo.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class OrderSeedConfig {

    @Bean
    CommandLineRunner seedOrders(OrderRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(Order.builder()
                    .orderNo("ORD-0001")
                    .customerName("ACME")
                    .pickupAddress("桃園市蘆竹區")
                    .deliveryAddress("台北市大安區")
                    .scheduledAt(LocalDateTime.now().plusHours(2))
                    .status(OrderStatus.CREATED)
                    .build());
            repo.save(Order.builder()
                    .orderNo("ORD-0002")
                    .customerName("FooBar")
                    .pickupAddress("新北市板橋區")
                    .deliveryAddress("新竹市東區")
                    .scheduledAt(LocalDateTime.now().plusHours(4))
                    .status(OrderStatus.ACCEPTED)
                    .build());
        };
    }
}
