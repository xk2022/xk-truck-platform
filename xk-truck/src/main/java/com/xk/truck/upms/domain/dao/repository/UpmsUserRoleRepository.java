package com.xk.truck.upms.domain.dao.repository;

import com.xk.truck.upms.domain.model.UpmsUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UpmsUserRoleRepository extends JpaRepository<UpmsUserRole, UUID> {
    Optional<UpmsUserRole> findByUserIdAndRoleCode(UUID userId, String roleCode);
}
