package com.xk.truck.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "t_order", indexes = {
        @Index(name = "idx_order_no", columnList = "order_no", unique = true),
        @Index(name = "idx_created_time", columnList = "created_time")
})
public class Order {

    @Id
    @Column(length = 36)
    private String uuid;

    @Comment("訂單編號")
    @Column(name = "order_no", length = 32, nullable = false, unique = true)
    private String orderNo;

    @Comment("客戶名稱")
    @Column(name = "customer_name", length = 64, nullable = false)
    private String customerName;

    @Comment("取件地址")
    @Column(name = "pickup_addr", length = 255, nullable = false)
    private String pickupAddress;

    @Comment("送達地址")
    @Column(name = "delivery_addr", length = 255, nullable = false)
    private String deliveryAddress;

    @Comment("預計時段（可空）")
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private OrderStatus status;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (status == null) status = OrderStatus.CREATED;
        var now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
