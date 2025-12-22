package com.xk.truck.tom.infra.persistence.entity;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ===============================================================
 * Entity : ImportOrderEntity
 * Layer  : Infrastructure / Persistence (TOM)
 * Purpose: 進口訂單明細（Import 專屬欄位）
 * ===============================================================
 * <p>
 * 設計：
 * - 與 OrderEntity 為 1:1（共用主鍵）
 * - 子表不可獨立存在（由 OrderEntity 管理 lifecycle）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tom_order_import")
@Schema(description = "TOM 進口訂單明細（共用主鍵）")
@DynamicInsert
@DynamicUpdate
public class ImportOrderEntity extends BaseEntity {

    // ===============================================================
    // Shared Primary Key
    // ===============================================================
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, nullable = false, updatable = false)
    @Schema(description = "進口訂單 UUID")
    private UUID uuid;

    /**
     * 共用主鍵關鍵：
     * - @MapsId：子表 uuid 直接使用 order.uuid
     * - @JoinColumn：子表的 FK 欄位名必須是 uuid（對齊上面的 @Column）
     */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uuid", nullable = false)
    private OrderEntity order;

    // ===============================================================
    // Import specific fields（依你 TS 類型錯誤提示先補齊）
    // ===============================================================

    @Comment("提貨/送貨單地點（例如：CY/CFS/倉庫）")
    @Column(name = "delivery_order_location", length = 64)
    private String deliveryOrderLocation;

    @Comment("通關日：進口放行時間（可空）")
    @Column(name = "customs_release_time")
    private LocalDateTime customsReleaseTime;

    @Comment("倉庫")
    @Column(name = "warehouse", length = 64)
    private String warehouse;

    @Comment("提單號（可空）")
    @Column(name = "bl_no", length = 32)
    private String blNo;

    @Comment("進口報關號")
    @Column(name = "import_decl_no", length = 32)
    private String importDeclNo;

    @Comment("到港通知/到貨文件備註（可空）")
    @Column(name = "arrival_notice", length = 500)
    private String arrivalNotice;

    // 你未來若有更多 import 專屬欄位（例如：報關行、放行號碼、驗櫃狀態...）
    // 建議都集中放在這裡

    // ===============================================================
    // Convenience
    // ===============================================================
    public void setOrder(OrderEntity order) {
        this.order = order;
        this.uuid = (order != null ? order.getUuid() : null);
    }
}
