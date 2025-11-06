package com.xk.truck.fms.domain.repo;

import com.xk.truck.fms.domain.model.Vehicle;
import com.xk.truck.fms.domain.model.VehicleStatus;
import com.xk.truck.fms.domain.model.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByPlateNo(String plateNo);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByType(VehicleType type, Pageable pageable);
}
