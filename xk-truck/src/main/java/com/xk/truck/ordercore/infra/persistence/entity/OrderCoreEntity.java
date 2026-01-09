package com.xk.truck.ordercore.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;

import com.xk.truck.ordercore.domain.model.OrderCoreStatus;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : OrderEntity
 * Module : order-core
 * Purpose: 跨 Domain 的「訂單核心（命運層）」
 * <p>
 * Responsibilities:
 * - 訂單唯一識別（UUID / OrderNo）
 * - 訂單高階狀態（OPEN / CANCELLED / CLOSED）
 * <p>
 * Non-Responsibilities:
 * - 不描述任何業務流程
 * - 不包含物流 / 電商 / 業務的 domain 狀態
 * ===============================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "order_core",
        indexes = {
                @Index(name = "idx_order_core_order_no", columnList = "order_no", unique = true),
                @Index(name = "idx_order_core_status", columnList = "order_status"),
                @Index(name = "idx_order_core_created_time", columnList = "created_time")
        }
)
public class OrderCoreEntity extends BaseEntity {

    // ===============================================================
    // Identity
    // ===============================================================

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "order_uuid", length = 36, nullable = false, updatable = false)
    private UUID orderUuid;

    @Comment("全系統訂單編號")
    @Column(name = "order_no", length = 32, nullable = false, unique = true)
    private String orderNo;

    // ===============================================================
    // Core Status (Fate)
    // ===============================================================

    @Comment("訂單高階狀態（命運層）")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 16, nullable = false)
    private OrderCoreStatus orderCoreStatus;

    // ===============================================================
    // Audit
    // ===============================================================

    @Comment("結案時間（CLOSED 時填寫）")
    @Column(name = "closed_at")
    private ZonedDateTime closedAt;

    @Comment("取消時間（CANCELLED 時填寫）")
    @Column(name = "cancelled_at")
    private ZonedDateTime cancelledAt;
}
