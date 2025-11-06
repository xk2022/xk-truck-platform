package com.xk.truck.fms.domain.repo;

import com.xk.truck.fms.domain.model.DispatchTask;
import com.xk.truck.fms.domain.model.DispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DispatchTaskRepository extends JpaRepository<DispatchTask, UUID> {

    List<DispatchTask> findByDriverIdAndStatus(UUID driverId, DispatchStatus status);

    List<DispatchTask> findByVehicleId(UUID vehicleId);

    List<DispatchTask> findByOrderId(UUID orderId);
}
