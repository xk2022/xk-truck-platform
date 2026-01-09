package com.xk.truck.adm.domain.model;

import com.xk.base.domain.model.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "adm_dict_category",
        uniqueConstraints = @UniqueConstraint(name = "uk_dict_cat_code", columnNames = "code"))
@Schema(description = "字典分類")
public class AdmDictCategory extends BaseEntity {

    // ===============================================================
    // Primary Key
    // ===============================================================
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uuid", length = 36, updatable = false, nullable = false, unique = true)
    @Schema(description = "字典分類 UUID")
    private UUID uuid;

    @Column(nullable = false, length = 50)
    @Comment("分類代碼（唯一）")
    private String code;        // 例：VEHICLE_TYPE、LICENSE_CLASS

    @Column(nullable = false, length = 100)
    @Comment("分類名稱")
    private String name;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 255)
    @Comment("說明")
    private String remark;
}
