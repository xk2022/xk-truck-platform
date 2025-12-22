// com/xk/truck/fms/domain/repository/VehicleRepository.java
package com.xk.truck.fms.domain.repository;

import com.xk.truck.fms.domain.model.Vehicle;
import com.xk.truck.fms.domain.model.VehicleStatus;
import com.xk.truck.fms.domain.model.VehicleType;
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
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlateNo(String plateNo);

    Optional<Vehicle> findByPlateNo(String plateNo);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByType(VehicleType type, Pageable pageable);

    long countByStatus(VehicleStatus status);

    long countByType(VehicleType type);
}
