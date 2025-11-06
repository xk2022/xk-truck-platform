package com.xk.truck.fms.domain.repo;

import com.xk.truck.fms.domain.model.Driver;
import com.xk.truck.fms.domain.model.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByPhone(String phone);

    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

    Page<Driver> findByOnDuty(boolean onDuty, Pageable pageable);
}
