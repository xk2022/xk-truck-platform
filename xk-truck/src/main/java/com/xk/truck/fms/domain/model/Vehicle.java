package com.xk.truck.fms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fms_vehicle", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fms_vehicle_plate", columnNames = "plate_no")
})
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "主鍵 UUID")
    private UUID id;

    @Column(name = "plate_no", nullable = false, length = 16)
    @Schema(description = "車牌")
    private String plateNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "車種")
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "車輛狀態")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(length = 50)
    private String brand;

    @Column(length = 50)
    private String model;

    @Column(name = "capacity_ton")
    private Double capacityTon;

    @Column(name = "current_driver_id", columnDefinition = "BINARY(16)")
    @Schema(description = "當前司機 UUID（MVP）")
    private UUID currentDriverId;
}
