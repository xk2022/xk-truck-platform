package com.xk.truck.fms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * 司機
 * - 採用 BINARY(16) UUID 作為主鍵
 * - 表名：fms_driver（與 FMS 模組對齊）
 * - 審計欄位繼承自 BaseEntity（createdBy/createdTime/updatedBy/updatedTime）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fms_driver", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fms_driver_phone", columnNames = "phone")
})
public class Driver extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    @Schema(description = "主鍵 UUID")
    private UUID id;

    @Column(nullable = false, length = 50)
    @Schema(description = "姓名", example = "王小明")
    private String name;

    @Column(nullable = false, length = 20)
    @Schema(description = "電話(唯一)", example = "0912000123")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 16)
    @Schema(description = "駕照類型", example = "LARGE")
    private DriverLicenseType licenseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "司機狀態", example = "ACTIVE")
    private DriverStatus status = DriverStatus.ACTIVE;

    @Column(name = "on_duty", nullable = false)
    @Schema(description = "是否上線中(可接任務)", example = "true")
    private boolean onDuty = false;

    @Column(name = "current_vehicle_id", columnDefinition = "BINARY(16)")
    @Schema(description = "當前綁定車輛 UUID（MVP）")
    private UUID currentVehicleId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;
}
