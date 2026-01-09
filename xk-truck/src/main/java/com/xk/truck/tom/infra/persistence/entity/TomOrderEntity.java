package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;
import com.xk.truck.tom.domain.model.TomOrderStatus;

import com.xk.truck.tom.domain.model.TomOrderType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : TomOrderEntity
 * Layer  : Infrastructure / Persistence (TOM)
 * Purpose: TOM 運輸訂單（流程層）
 * ===============================================================
 * <p>
 * Design Notes:
 * - 與 order-core 共享主鍵：orderUuid
 * - TOM 只描述運輸訂單流程（NEW/ACCEPTED/ASSIGNED）
 * - customerName 為快照，避免跨域 join，確保歷史不漂移
 * - note 為業務備註（BaseEntity.remark 保留做系統備註）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tom_order",
        indexes = {
                @Index(name = "idx_tom_order_order_no", columnList = "order_no", unique = true),
                @Index(name = "idx_tom_order_tom_status", columnList = "tom_status"),
                @Index(name = "idx_tom_order_customer_uuid", columnList = "customer_uuid"),
                @Index(name = "idx_tom_order_created_time", columnList = "created_time")
        }
)
@Schema(description = "TOM 訂單主表（流程層）")
public class TomOrderEntity extends BaseEntity {

    // ===============================================================
    // Shared Primary Key (same as order-core.order_uuid)
    // ===============================================================

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "order_uuid", length = 36, nullable = false, updatable = false)
    @Schema(description = "訂單 UUID（與 order-core 共享）")
    private UUID orderUuid;

    // ===============================================================
    // Identifiers / Type
    // ===============================================================

    @Comment("全系統訂單編號（與 order-core 同步）")
    @Column(name = "order_no", length = 32, nullable = false, unique = true)
    private String orderNo;

    @Comment("訂單類型（IMPORT/EXPORT/LOCAL）")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 16, nullable = false)
    private TomOrderType orderType;

    // ===============================================================
    // TOM Status (Process)
    // ===============================================================

    @Comment("TOM 流程狀態（NEW/ACCEPTED/ASSIGNED）")
    @Enumerated(EnumType.STRING)
    @Column(name = "tom_status", length = 16, nullable = false)
    private TomOrderStatus tomStatus;

    // ===============================================================
    // Customer Snapshot
    // ===============================================================

    @Comment("客戶 UUID")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "customer_uuid", length = 36, nullable = false)
    private UUID customerUuid;

    @Comment("客戶名稱（快照）")
    @Column(name = "customer_name", length = 200, nullable = false)
    private String customerName;

    // ===============================================================
    // Route (MVP: single pickup & delivery)
    // ===============================================================

    @Comment("取件地址")
    @Column(name = "pickup_address", length = 255, nullable = false)
    private String pickupAddress;

    @Comment("送達地址")
    @Column(name = "delivery_address", length = 255, nullable = false)
    private String deliveryAddress;

    // ===============================================================
    // Schedule / Reference / Note
    // ===============================================================

    @Comment("預計時段（可空）")
    @Column(name = "scheduled_at")
    private ZonedDateTime scheduledAt;

    @Comment("客戶參考號（可空）")
    @Column(name = "customer_ref_no", length = 64)
    private String customerRefNo;

    @Comment("備註（可空）")
    @Column(name = "remark", length = 500)
    private String remark;
}
