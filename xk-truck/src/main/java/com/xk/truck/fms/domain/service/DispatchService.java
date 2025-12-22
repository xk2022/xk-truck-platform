package com.xk.truck.fms.domain.service;

import com.xk.truck.fms.domain.model.DispatchStatus;
import com.xk.truck.fms.domain.model.DispatchTask;
import com.xk.truck.fms.domain.repository.DispatchTaskRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DispatchService {

    private final DispatchTaskRepository repo;

    public DispatchTask assign(UUID orderId, UUID vehicleId, UUID driverId) {
        DispatchTask task = DispatchTask.builder()
                .orderId(orderId)
                .vehicleId(vehicleId)
                .driverId(driverId)
                .status(DispatchStatus.ASSIGNED)
                .assignedTime(ZonedDateTime.now())
                .build();
        return repo.save(task);
    }

    public DispatchTask start(UUID taskId) {
        DispatchTask task = find(taskId);
        task.setStatus(DispatchStatus.IN_PROGRESS);
        task.setStartedTime(ZonedDateTime.now());
        return task;
    }

    public DispatchTask sign(UUID taskId) {
        DispatchTask task = find(taskId);
        task.setStatus(DispatchStatus.SIGNED);
        task.setSignedTime(ZonedDateTime.now());
        return task;
    }

    public DispatchTask complete(UUID taskId) {
        DispatchTask task = find(taskId);
        task.setStatus(DispatchStatus.COMPLETED);
        task.setCompletedTime(ZonedDateTime.now());
        return task;
    }

    public DispatchTask cancel(UUID taskId) {
        DispatchTask task = find(taskId);
        task.setStatus(DispatchStatus.CANCELLED);
        return task;
    }

    public DispatchTask find(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DispatchTask not found: " + id));
    }
}
