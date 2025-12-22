// com/xk/truck/fms/domain/repository/DriverRepository.java
package com.xk.truck.fms.domain.repository;

import com.xk.truck.fms.domain.model.Driver;
import com.xk.truck.fms.domain.model.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: RoleRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 Role（角色）實體的 CRUD 與查詢
 * Notes    :
 * - 角色以 code 唯一識別（如：ADMIN, DISPATCH, DRIVER）
 * - 可用於角色管理與授權模組
 * ===============================================================
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByPhone(String phone);

    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

    Page<Driver> findByOnDuty(boolean onDuty, Pageable pageable);

    boolean existsByUserId(UUID userId);

    Optional<Driver> findByUserId(UUID userId);
}
