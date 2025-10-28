package com.xk.exampleFolder.api;

import com.xk.exampleFolder.domain.order.OrderRepository;
import com.xk.exampleFolder.domain.order.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository repo;

    public OrderController(OrderRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public OrderEntity create(@RequestBody OrderEntity in) {
        return repo.save(in);
    }

    @GetMapping("/{orderNo}")
    public OrderEntity get(@PathVariable String orderNo) {
        return repo.findByOrderNo(orderNo).orElseThrow();
    }
}
