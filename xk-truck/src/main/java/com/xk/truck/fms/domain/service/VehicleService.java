package com.xk.truck.fms.domain.service;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.fms.controller.api.dto.VehicleCreateReq;
import com.xk.truck.fms.controller.api.dto.VehicleResp;
import com.xk.truck.fms.domain.model.Vehicle;
import com.xk.truck.fms.domain.model.VehicleStatus;
import com.xk.truck.fms.domain.repo.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 預設所有方法為唯讀；需要寫入的在方法上覆蓋
public class VehicleService {

    private final VehicleRepository repository;

    /**
     * 建立車輛
     * - 使用 XkBeanUtils 將請求 DTO 複製成 Entity
     * - 直接 repository.save() 後回傳 Resp DTO
     * - 若 plateNo 設為 UNIQUE，重複會拋 DataIntegrityViolationException
     */
    @Transactional
    public VehicleResp create(VehicleCreateReq request) {
        repository.findByPlateNo(request.getPlateNo()).ifPresent(v -> {
            throw new IllegalArgumentException("車牌已存在: " + request.getPlateNo());
        });

        Vehicle entity = XkBeanUtils.copyProperties(request, Vehicle::new);
        Vehicle saved = repository.save(entity);
        return XkBeanUtils.copyProperties(saved, VehicleResp::new);
    }

    /**
     * 取得單筆
     */
    public VehicleResp findById(UUID id) {
        Vehicle entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + id));
        return XkBeanUtils.copyProperties(entity, VehicleResp::new);
    }

    /**
     * 分頁查詢（可直接加上 pageable 的 sort）
     */
    public Page<VehicleResp> list(Pageable pageable) {
        Page<Vehicle> entities = repository.findAll(pageable);
        return entities.map(e -> XkBeanUtils.copyProperties(e, VehicleResp::new));
    }

    /**
     * 全量更新
     * ⚠️ 原本寫法「new Entity + setId + save」屬於 upsert/覆蓋式更新，容易覆蓋掉未在請求中的欄位（如 auditing 欄位）
     * → 這裡改為「讀出舊資料，再合併變更」的安全寫法（JPA 髒值偵測會自動 flush）
     */
    @Transactional
    public VehicleResp update(UUID id, VehicleCreateReq updateData) {
        Vehicle entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + id));

        // 合併允許更新的欄位（避免覆蓋到不該變的欄位）
        entity.setPlateNo(updateData.getPlateNo());
        entity.setType(updateData.getType());
        entity.setBrand(updateData.getBrand());
        entity.setModel(updateData.getModel());
        entity.setCapacityTon(updateData.getCapacityTon());
        // status 不在此處改（避免不小心改變狀態）；請用 updateStatus()
//        Vehicle entity = XkBeanUtils.copyProperties(updateData, Vehicle::new);
//        entity.setId(id);
//        Vehicle saved = repository.save(entity);
//        return XkBeanUtils.copyProperties(saved, VehicleResp::new);

        // 這裡不必再呼叫 save()：持久化狀態 + @Transactional 會自動 flush
        return XkBeanUtils.copyProperties(entity, VehicleResp::new);
    }

    /**
     * 單一欄位：更新狀態
     * - 保持接口語義清楚
     * - 只改動 status，其他欄位不動
     */
    @Transactional
    public VehicleResp updateStatus(UUID id, VehicleStatus status) {
        Vehicle entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + id));

        // 可選：狀態轉移規則（ex. INACTIVE -> BUSY 禁止）
        // validateStatusTransition(entity.getStatus(), status);
        entity.setStatus(status);
        return XkBeanUtils.copyProperties(entity, VehicleResp::new);
    }

    /**
     * 刪除（MVP 先做實體刪除；之後可改軟刪除）
     * 回傳 true 表示刪除成功、false 表示目標不存在
     */
    @Transactional
    public boolean delete(UUID id) {
        return repository.findById(id)
                .map(entity -> {
                    repository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    // --------------------------------------------
    // 可選：狀態轉移規則（未用到時可刪）
    // --------------------------------------------
    private void validateStatusTransition(VehicleStatus from, VehicleStatus to) {
        if (from == VehicleStatus.INACTIVE && to == VehicleStatus.BUSY) {
            throw new IllegalStateException("停用車輛不可直接變更為 BUSY");
        }
    }
}
