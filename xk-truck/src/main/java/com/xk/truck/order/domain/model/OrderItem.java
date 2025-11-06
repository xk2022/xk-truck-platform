package com.xk.truck.order.domain.model;

import com.xk.base.domain.model.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "t_order_item")
public class OrderItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 150, nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer qty;

    @Column(length = 32)
    private String unit;

    @Column(length = 255)
    private String remark;
}
