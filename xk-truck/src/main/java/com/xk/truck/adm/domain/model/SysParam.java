package com.xk.truck.adm.domain.model;

import com.xk.base.domain.model.BaseEntity;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.Comment;

import java.util.UUID;

@Data
@Entity
@Table(name = "sys_param"
        , uniqueConstraints = @UniqueConstraint(name = "uk_param_key", columnNames = "param_key"))
public class SysParam extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "param_key", nullable = false, length = 100)
    @Comment("參數鍵（唯一）")
    private String key;

    @Column(name = "param_value", nullable = false, length = 500)
    @Comment("參數值")
    private String value;

    @Column(length = 255)
    @Comment("說明")
    private String description;
}
