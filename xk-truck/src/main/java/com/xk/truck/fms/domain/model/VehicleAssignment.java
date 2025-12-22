package com.xk.truck.fms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * （車輛指派紀錄 / 履歷）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fms_vehicle_assignment", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fms_vehicle_plate", columnNames = "plate_no")
})
public class VehicleAssignment extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "主鍵 UUID")
    private UUID uuid;

    private UUID vehicleId;
    private UUID driverId;          // 司機
    private UUID orderId;           // 產生於某張訂單（選配）

    private LocalDateTime startTime;
    private LocalDateTime endTime;  // 若為 null 代表目前仍在使用中

    private String note;
}
