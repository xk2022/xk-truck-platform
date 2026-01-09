package com.xk.truck.adm.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table(name = "adm_dict_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_dict_item_cat_code", columnNames = {"category_id", "code"}))
@Schema(description = "字典項目")
public class AdmDictItem extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "字典項目 UUID")
    private UUID uuid;

    @Column(name = "category_uuid", nullable = false)
    @Comment("所屬分類")
    private UUID categoryUuid;

    @Column(nullable = false, length = 50)
    @Comment("項目代碼（分類內唯一）")
    private String itemCode;    // 例：TRAILER、HEAD、SMALL_TRUCK

    @Column(nullable = false, length = 100)
    @Comment("顯示名稱")
    private String itemLabel;    // 例：半掛拖車、車頭、小貨車

    @Column(nullable = false)
    @Comment("排序")
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Comment("是否啟用")
    private Boolean enabled = true;

    @Column(length = 255)
    @Comment("說明")
    private String remark;
}
