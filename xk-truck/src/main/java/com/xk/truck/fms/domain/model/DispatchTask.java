package com.xk.truck.fms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dispatch_task")
public class DispatchTask extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    @Column(name = "vehicle_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID vehicleId;

    @Column(name = "driver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DispatchStatus status = DispatchStatus.CREATED;

    // 時間節點記錄，用來回頭查
    private ZonedDateTime assignedTime;
    private ZonedDateTime startedTime;
    private ZonedDateTime signedTime;
    private ZonedDateTime completedTime;
}
