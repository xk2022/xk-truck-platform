package com.xk.truck.adm.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Comment;

import java.util.UUID;

@Data
@Entity
@Table(name = "dict_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_dict_item_cat_code", columnNames = {"category_id", "code"}))
@Schema(description = "字典項目")
public class DictItem extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    @Comment("PK")
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Comment("所屬分類")
    private DictCategory category;

    @Column(nullable = false, length = 50)
    @Comment("項目代碼（分類內唯一）")
    private String code;    // 例：TRAILER、HEAD、SMALL_TRUCK

    @Column(nullable = false, length = 100)
    @Comment("顯示名稱")
    private String name;    // 例：半掛拖車、車頭、小貨車

    @Column(length = 255)
    @Comment("說明")
    private String description;

    @Column(nullable = false)
    @Comment("排序")
    private Integer sortNo = 0;

    @Column(nullable = false)
    @Comment("是否啟用")
    private Boolean enabled = true;
}
