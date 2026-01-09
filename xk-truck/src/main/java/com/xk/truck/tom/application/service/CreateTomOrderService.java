package com.xk.truck.tom.application.service;

import com.xk.base.exception.BusinessException;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.ordercore.application.dto.OrderCoreResult;
import com.xk.truck.ordercore.application.dto.cmd.CreateOrderCoreCommand;
import com.xk.truck.ordercore.application.port.in.CreateOrderCoreUseCase;
import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.CreateTomOrderCommand;
import com.xk.truck.tom.application.port.in.CreateTomOrderUseCase;
import com.xk.truck.tom.application.port.out.OrderNoGeneratorPort;
import com.xk.truck.tom.domain.model.TomOrder;
import com.xk.truck.tom.application.port.out.TomOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ===============================================================
 * UseCase Impl: CreateTomOrderService
 * Layer       : Application (Use Case)
 * Role        : Transaction Script / Orchestration
 * ===============================================================
 * <p>
 * Responsibilities:
 * - 產生訂單編號（OrderNoGeneratorPort）
 * - 呼叫 order-core 建立核心訂單（CreateOrderUseCase）
 * - 建立 TOM Aggregate（TomOrder.create）
 * - Persist TOM（TomOrderRepository）
 * - 處理技術性可靠度策略（例如：訂單號撞 unique 時重試）
 * <p>
 * Notes:
 * - 假設 order-core 與 TOM 同 DB / 同 transaction manager，才能用單一 @Transactional
 * ===============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTomOrderService implements CreateTomOrderUseCase {

    private static final int MAX_RETRY = 3;

    private final TomOrderRepository tomOrderRepository;
    private final OrderNoGeneratorPort orderNoGenerator;

    // 把 order-core 當服務（inbound port）
    private final CreateOrderCoreUseCase createOrderCoreUseCase;

    @Override
    @Transactional
    public TomOrderResult execute(CreateTomOrderCommand cmd) {

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return doCreate(cmd);
            } catch (DataIntegrityViolationException ex) {
                // 目前先保守：假設撞 unique（orderNo）才重試
                // 企業級可再精準判斷 constraint name / SQL state
                if (attempt >= MAX_RETRY) {
                    throw new BusinessException(
                            "ORDER_NO_GENERATE_FAILED",
                            "訂單編號產生失敗，請稍後再試",
                            Map.of("retry", attempt)
                    );
                }
                log.warn("[TOM][CREATE] possible orderNo collision, retry {}/{}", attempt, MAX_RETRY, ex);
            }
        }

        throw new BusinessException("ORDER_CREATE_FAILED", "建立訂單失敗");
    }

    private TomOrderResult doCreate(CreateTomOrderCommand cmd) {

        // 1) 產生訂單編號（可能撞號）
        final String orderNo = orderNoGenerator.nextTomOrderNo();

        // 2) 建立 order-core（命運層）
        OrderCoreResult core = createOrderCoreUseCase.execute(new CreateOrderCoreCommand(orderNo));

        // 3) 建立 TOM Aggregate（流程層）
        TomOrder tom = TomOrder.create(
                core.getOrderUuid(),
                core.getOrderNo(),
                cmd.getOrderType(),
                cmd.getCustomerUuid(),
                cmd.getCustomerName(),
                cmd.getPickupAddress(),
                cmd.getDeliveryAddress(),
                cmd.getScheduledAt(),
                cmd.getCustomerRefNo(),
                cmd.getRemark()
        );

        // 4) Persist TOM
        tom = tomOrderRepository.save(tom);

        // 5) 回傳結果（建議由 Result.from 統一輸出）
//        return TomOrderResult.from(tom, List.of("ACCEPT", "CANCEL"));
        return XkBeanUtils.copyProperties(tom, TomOrderResult::new);
    }
}
