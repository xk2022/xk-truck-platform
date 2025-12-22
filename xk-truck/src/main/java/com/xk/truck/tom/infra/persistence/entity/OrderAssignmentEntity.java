package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : OrderAssignmentEntity
 * Purpose: 訂單派單歷程（車輛/司機）
 * ===============================================================
 * - 一張訂單可能會多次派單（改派/撤派/重新指派）
 * - 這張表用來保留歷史
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tom_order_assignment",
        indexes = {
                @Index(name = "idx_tom_assign_order_uuid", columnList = "order_uuid"),
                @Index(name = "idx_tom_assign_vehicle_uuid", columnList = "vehicle_uuid"),
                @Index(name = "idx_tom_assign_driver_uuid", columnList = "driver_uuid"),
                @Index(name = "idx_tom_assign_time", columnList = "assigned_time")
        }
)
@Schema(description = "TOM 訂單派單歷程")
@DynamicInsert
@DynamicUpdate
public class OrderAssignmentEntity extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "派單歷程 UUID")
    private UUID uuid;

    // ===============================================================
    // FK (Order)
    // ===============================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_uuid", nullable = false)
    private OrderEntity order;

    // ===============================================================
    // Assigned Targets
    // ===============================================================
    @Comment("指派車輛ID（對應 FMS vehicle uuid）")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "vehicle_uuid", length = 36)
    private UUID vehicleUuid;

    @Comment("指派司機ID（對應 FMS driver uuid）")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "driver_uuid", length = 36)
    private UUID driverUuid;

    @Comment("指派時間")
    @Column(name = "assigned_time", nullable = false)
    private ZonedDateTime assignedTime;

    @Column(name = "ended_time")
    private ZonedDateTime endedTime;

    @Comment("指派備註")
    @Column(name = "note", length = 500)
    private String note;
}
