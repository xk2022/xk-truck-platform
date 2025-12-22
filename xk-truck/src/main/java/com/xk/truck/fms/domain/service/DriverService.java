package com.xk.truck.fms.domain.service;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.fms.controller.api.dto.DriverCreateReq;
import com.xk.truck.fms.controller.api.dto.DriverResp;
import com.xk.truck.fms.domain.model.Driver;
import com.xk.truck.fms.domain.model.DriverStatus;
import com.xk.truck.fms.domain.repository.DriverRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 司機服務
 * - 預設 readOnly；寫入方法標記 @Transactional
 * - create() 防重 phone
 * - update() 使用「讀出 + 合併」避免覆蓋不該變更欄位
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository repository;

    @Transactional
    public DriverResp create(DriverCreateReq req) {
        repository.findByPhone(req.getPhone()).ifPresent(d -> {
            throw new IllegalArgumentException("電話已存在: " + req.getPhone());
        });

        Driver entity = XkBeanUtils.copyProperties(req, Driver::new);
        Driver saved = repository.save(entity);
        return XkBeanUtils.copyProperties(saved, DriverResp::new);
    }

    public DriverResp findById(UUID id) {
        Driver entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + id));
        return XkBeanUtils.copyProperties(entity, DriverResp::new);
    }

    public Page<DriverResp> list(Pageable pageable) {
        return repository.findAll(pageable).map(e -> XkBeanUtils.copyProperties(e, DriverResp::new));
    }

    @Transactional
    public DriverResp update(UUID id, DriverCreateReq update) {
        Driver entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + id));

        entity.setName(update.getName());
        entity.setPhone(update.getPhone());
        entity.setLicenseType(update.getLicenseType());
        // status、onDuty 不在此更新（請使用專屬 API）
        return XkBeanUtils.copyProperties(entity, DriverResp::new);
    }

    @Transactional
    public DriverResp updateStatus(UUID id, DriverStatus status) {
        Driver entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + id));
        entity.setStatus(status);
        // 可加：狀態轉移規則驗證
        return XkBeanUtils.copyProperties(entity, DriverResp::new);
    }

    @Transactional
    public DriverResp updateOnDuty(UUID id, boolean onDuty) {
        Driver entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + id));
        entity.setOnDuty(onDuty);
        return XkBeanUtils.copyProperties(entity, DriverResp::new);
    }

    @Transactional
    public boolean delete(UUID id) {
        return repository.findById(id)
                .map(e -> {
                    repository.delete(e);
                    return true;
                })
                .orElse(false);
    }
}
