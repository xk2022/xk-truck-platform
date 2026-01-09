package com.xk.truck.ordercore.application.service;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.ordercore.application.dto.OrderCoreResult;
import com.xk.truck.ordercore.application.dto.cmd.CreateOrderCoreCommand;
import com.xk.truck.ordercore.application.port.in.CreateOrderCoreUseCase;
import com.xk.truck.ordercore.application.port.out.OrderCoreRepository;
import com.xk.truck.ordercore.domain.model.OrderCore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ===============================================================
 * UseCase Impl: CreateOrderCoreService
 * Layer       : Application (Use Case)
 * Role        : Transaction Script / Orchestrator
 * ===============================================================
 * <p>
 * Responsibilities:
 * - 定義 order-core「建立訂單」的交易邊界
 * - 建立初始狀態為 OPEN 的核心訂單（命運層）
 * - 回傳跨 Domain 可用的最小結果（UUID / No / Status）
 * <p>
 * Design Notes:
 * - 本 UseCase 以 Domain Aggregate 為核心，不直接操作 JPA Entity
 * - DB unique constraint 為 orderNo 唯一性的最終保證
 * - 技術性例外（如撞號）交由呼叫端（如 TOM Orchestrator）處理
 * <p>
 * Non-Responsibilities:
 * - 不產生訂單編號（由外部系統 / Orchestrator 負責）
 * - 不處理重試策略
 * - 不處理跨 Domain 的流程協調
 * ===============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderCoreService implements CreateOrderCoreUseCase {

    private final OrderCoreRepository orderCoreRepository;

    @Override
    @Transactional
    public OrderCoreResult execute(CreateOrderCoreCommand cmd) {

        // Fast-fail 檢查（非一致性保證，僅提升錯誤可讀性）
        if (orderCoreRepository.existsByOrderNo(cmd.getOrderNo())) {
            throw new BusinessException("ORDER_NO_EXISTS", "訂單編號已存在");
        }

        try {
            // 建立 Domain Aggregate（命運層）
            OrderCore aggregate = OrderCore.create(cmd.getOrderNo());
            OrderCore saved = orderCoreRepository.save(aggregate);

            // 回傳 Application Result
            return XkBeanUtils.copyProperties(saved, OrderCoreResult::new);

        } catch (DataIntegrityViolationException ex) {
            // 刻意不包裝，讓上層（如 TOM）決定是否 retry
            log.warn("[ORDER-CORE][CREATE] orderNo collision: {}", cmd.getOrderNo(), ex);
            throw ex;
        }
    }
}
