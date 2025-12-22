package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;
import com.xk.truck.tom.domain.model.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * ===============================================================
 * Entity : OrderStatusLogEntity
 * Purpose: 訂單狀態歷程
 * ===============================================================
 * - 一張訂單可有多筆狀態變更記錄
 * - 用來追蹤：何時從哪個狀態 -> 哪個狀態、原因、操作者
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tom_order_status_log",
        indexes = {
                @Index(name = "idx_tom_osl_order_uuid", columnList = "order_uuid"),
                @Index(name = "idx_tom_osl_created_time", columnList = "created_time"),
                @Index(name = "idx_tom_osl_to_status", columnList = "to_status")
        }
)
@Schema(description = "TOM 訂單狀態歷程")
@DynamicInsert
@DynamicUpdate
public class OrderStatusLogEntity extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "狀態歷程 UUID")
    private UUID uuid;

    // ===============================================================
    // FK (Order)
    // ===============================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_uuid", nullable = false)
    private OrderEntity order;

    // ===============================================================
    // Status
    // ===============================================================
    @Comment("原狀態")
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private OrderStatus fromStatus;

    @Comment("新狀態")
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 16, nullable = false)
    private OrderStatus toStatus;

    @Comment("變更原因/備註")
    @Column(name = "reason", length = 500)
    private String reason;
}
