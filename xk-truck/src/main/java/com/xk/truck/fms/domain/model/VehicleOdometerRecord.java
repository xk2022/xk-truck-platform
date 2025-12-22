package com.xk.truck.fms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 里程紀錄
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fms_vehicle_odometer_record", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fms_vehicle_plate", columnNames = "plate_no")
})
public class VehicleOdometerRecord extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "主鍵 UUID")
    private UUID uuid;

    private UUID vehicleId;

    private LocalDateTime recordTime;
    private Long odometer;        // 當時里程數（km）
    private String source;        // MANUAL / ORDER_COMPLETE / MAINTENANCE
}
