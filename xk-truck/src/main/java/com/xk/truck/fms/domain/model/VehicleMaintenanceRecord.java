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
@Entity
@Table(name = "fms_vehicle_maintenance_record", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fms_vehicle_plate", columnNames = "plate_no")
})
public class VehicleMaintenanceRecord extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "主鍵 UUID")
    private UUID uuid;

    private UUID vehicleId;

    private LocalDateTime maintenanceTime;
    private Long odometerAtMaintenance;   // 當時里程
    private String type;                  // 定期保養 / 修理 / 檢驗
    private String description;           // 內容簡述
    private Integer cost;                 // 花費（可選）

    private String workshop;              // 維修廠名稱
    private String invoiceNumber;         // 單號（選配）

    private LocalDateTime nextMaintenanceTime; // 下次保養預估（選配）
}
