package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;
import com.xk.truck.tom.domain.model.OrderStatus;

import com.xk.truck.tom.domain.model.OrderType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.*;

import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : OrderEntity
 * Layer  : Infrastructure / Persistence (TOM)
 * Purpose: 訂單主表（共用欄位）
 * ===============================================================
 * <p>
 * 設計說明：
 * - 作為「Order Aggregate Root」的持久化映射
 * - 僅負責資料結構，不承擔複雜商業邏輯
 * - Import / Export 差異欄位下放至子表（1:1）
 * <p>
 * 關聯：
 * - 1:1 ImportOrderEntity / ExportOrderEntity（共用主鍵）
 * - 1:N OrderAssignmentEntity（派單歷程）
 * - 1:N OrderStatusLogEntity（狀態歷程）
 * <p>
 * 注意：
 * - 不使用 @Data（避免 lazy loading / toString 問題）
 * - 不跨 bounded context（customer / driver / vehicle 僅存 ID）
 * <p>
 * @author yuan Created on 2025/12/18.
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
                @Index(name = "idx_tom_order_customer_uuid", columnList = "customer_uuid"),
                @Index(name = "idx_tom_order_status", columnList = "status"),
                @Index(name = "idx_tom_order_created_time", columnList = "created_time")
        }
)
@Schema(description = "TOM 訂單主表（共用欄位）")
public class OrderEntity extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "訂單 UUID")
    private UUID uuid;

    // ===============================================================
    // Core fields (穩定識別)
    // ===============================================================

    @Comment("訂單編號")
    @Column(name = "order_no", length = 32, nullable = false, unique = true)
    private String orderNo;

    @Comment("訂單類型")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 16, nullable = false)
    private OrderType orderType;

    @Comment("訂單狀態")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private OrderStatus orderStatus;

    // ===============================================================
    // Customer / Assignment (先用 id，避免跨 bounded context 耦合)
    // ===============================================================

    @Comment("客戶ID")
    @Column(name = "customer_uuid", nullable = false)
    private UUID customerUuid;

    @Comment("指派車輛ID")
    @Column(name = "vehicle_uuid")
    private UUID vehicleUuid;

    @Comment("指派司機ID")
    @Column(name = "driver_uuid")
    private UUID driverUuid;

    // ===============================================================
    // Address / Schedule (共用)
    // ===============================================================

    @Comment("取件地址")
    @Column(name = "pickup_addr", length = 255, nullable = false)
    private String pickupAddress;

    @Comment("送達地址")
    @Column(name = "delivery_addr", length = 255, nullable = false)
    private String deliveryAddress;

    @Comment("預計時段（可空）")
    @Column(name = "scheduled_at")
    private ZonedDateTime scheduledAt;

    // ===============================================================
    // Shipping / Container (你先放 base 沒問題，但之後可移到 detail)
    // ===============================================================

    @Comment("船公司")
    @Column(name = "shipping_company", length = 64)
    private String shippingCompany;

    @Comment("船名/航次")
    @Column(name = "vessel_voyage", length = 64)
    private String vesselVoyage;

    @Comment("櫃號")
    @Column(name = "container_no", length = 16)
    private String containerNo;

    @Comment("櫃型（20GP/40HQ...）")
    @Column(name = "container_type", length = 16)
    private String containerType;

    @Comment("件數")
    @Column(name = "package_qty")
    private Integer packageQty;

    @Comment("毛重")
    @Column(name = "gross_weight", precision = 12, scale = 3)
    private BigDecimal grossWeight;

    @Comment("CBM")
    @Column(name = "cbm", precision = 12, scale = 3)
    private BigDecimal cbm;

    @Comment("裝貨港 POL")
    @Column(name = "pol", length = 64)
    private String pol;

    @Comment("卸貨港 POD")
    @Column(name = "pod", length = 64)
    private String pod;

    @Comment("ETD")
    @Column(name = "etd")
    private LocalDateTime etd;

    @Comment("ETA")
    @Column(name = "eta")
    private LocalDateTime eta;

    @Comment("備註")
    @Column(name = "note", length = 500)
    private String note;

    // ===============================================================
    // 1:1 Detail (Import / Export) - 共用主鍵
    // 注意：mappedBy 欄位必須對應子表的 @OneToOne @MapsId 欄位名稱
    // ===============================================================

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ImportOrderEntity importDetail;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ExportOrderEntity exportDetail;

    // ===============================================================
    // 1:N Logs
    // ===============================================================

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdTime DESC")
    private List<OrderStatusLogEntity> statusLogs = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("assignedTime DESC")
    private List<OrderAssignmentEntity> assignments = new ArrayList<>();

    // ===============================================================
    // Convenience Methods (避免 service 到處 set)
    // ===============================================================
    public void attachImportDetail(ImportOrderEntity detail) {
        this.importDetail = detail;
        if (detail != null) detail.setOrder(this);
    }

    public void attachExportDetail(ExportOrderEntity detail) {
        this.exportDetail = detail;
        if (detail != null) detail.setOrder(this);
    }

    public void changeStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }

    public void addStatusLog(OrderStatus from, OrderStatus to, String reason) {
        OrderStatusLogEntity log = OrderStatusLogEntity.builder()
                .order(this)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .build();
        this.statusLogs.add(log);
    }

    public void addAssignment(UUID vehicleUuid, UUID driverUuid, String note) {
        OrderAssignmentEntity a = OrderAssignmentEntity.builder()
                .order(this)
                .vehicleUuid(vehicleUuid)
                .driverUuid(driverUuid)
                .note(note)
                .build();
        this.assignments.add(a);

        // 同步主表（方便查詢當前指派）
        this.vehicleUuid = vehicleUuid;
        this.driverUuid = driverUuid;
    }

}
