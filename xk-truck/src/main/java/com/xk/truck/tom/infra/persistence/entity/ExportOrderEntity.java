package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : ExportOrderEntity
 * Layer  : Infrastructure / Persistence (TOM)
 * Purpose: 出口訂單明細（Export 專屬欄位）
 * ===============================================================
 * <p>
 * 設計：
 * - 與 OrderEntity 1:1（共用主鍵）
 * - 子表由主表 OrderEntity 管理
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tom_order_export")
@Schema(description = "TOM 出口訂單明細（共用主鍵）")
@DynamicInsert
@DynamicUpdate
public class ExportOrderEntity extends BaseEntity {

    // ===============================================================
    // Shared Primary Key
    // ===============================================================
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, nullable = false, updatable = false)
    @Schema(description = "出口訂單 UUID")
    private UUID uuid;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uuid", nullable = false)
    private OrderEntity order;

    // ===============================================================
    // Export specific fields（你可依實際業務調整）
    // ===============================================================

    @Comment("訂艙號")
    @Column(name = "booking_no", length = 32)
    private String bookingNo;

    @Comment("裝櫃日")
    @Column(name = "stuffing_date")
    private LocalDateTime stuffingDate;

    @Comment("結關時間（可空）")
    @Column(name = "cutoff_time")
    private LocalDateTime cutoffTime;

    @Comment("出口報關號")
    @Column(name = "export_decl_no", length = 32)
    private String exportDeclNo;

    @Comment("裝櫃地點（可空）")
    @Column(name = "stuffing_location", length = 128)
    private String stuffingLocation;

    @Comment("截關日")
    @Column(name = "cut_off_date")
    private LocalDateTime cutOffDate;

    @Comment("S/O No（可空）")
    @Column(name = "so_no", length = 32)
    private String soNo;

    @Comment("報關行（可空）")
    @Column(name = "customs_broker", length = 64)
    private String customsBroker;

    // ===============================================================
    // Convenience
    // ===============================================================
    public void setOrder(OrderEntity order) {
        this.order = order;
        this.uuid = (order != null ? order.getUuid() : null);
    }
}
