package com.xk.truck.adm.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Comment;

import java.util.UUID;

@Data
@Entity
@Table(name = "dict_category",
        uniqueConstraints = @UniqueConstraint(name = "uk_dict_cat_code", columnNames = "code"))
@Schema(description = "字典分類")
public class DictCategory extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    @Comment("PK")
    private UUID id;

    @Column(nullable = false, length = 50)
    @Comment("分類代碼（唯一）")
    private String code;        // 例：VEHICLE_TYPE、LICENSE_CLASS

    @Column(nullable = false, length = 100)
    @Comment("分類名稱")
    private String name;

    @Column(length = 255)
    @Comment("說明")
    private String description;
}
