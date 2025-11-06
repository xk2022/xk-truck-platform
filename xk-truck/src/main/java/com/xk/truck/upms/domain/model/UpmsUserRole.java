package com.xk.truck.upms.domain.model;

import com.xk.base.domain.model.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "upms_user_role", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_role", columnNames = {"user_id", "role_code"})
})
public class UpmsUserRole extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;
}
