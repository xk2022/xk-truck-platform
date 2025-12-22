package com.xk.truck.tom.application.usecase.impl;

import com.xk.base.exception.BusinessException;
import com.xk.truck.tom.application.mapper.ImportOrderMapper;
import com.xk.truck.tom.application.usecase.cmd.CreateImportOrderCmd;
import com.xk.truck.tom.application.usecase.CreateImportOrderUseCase;
import com.xk.truck.tom.application.usecase.dto.OrderResp;
import com.xk.truck.tom.domain.model.CreateImportOrderSpec;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.domain.service.OrderDomainService;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ===============================================================
 * UseCase Impl : CreateImportOrderUseCaseImpl
 * Layer        : Application
 * Purpose      : 建立進口訂單（IMPORT）
 * ===============================================================
 * <p>
 * 流程：
 * 1. 驗證 Command
 * 2. 呼叫 DomainService.createImport()
 * 3. 回傳 OrderResp
 * ===============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateImportOrderUseCaseImpl implements CreateImportOrderUseCase {

    private final OrderDomainService domainService;
    private final ImportOrderMapper importOrderMapper;

    @Override
    public OrderResp execute(CreateImportOrderCmd cmd) {
        if (cmd == null) {
            throw new BusinessException("TOM_IMPORT_CMD_EMPTY", "建立進口訂單請求不得為空");
        }
        if (cmd.getCustomerUuid() == null) {
            throw new BusinessException("TOM_CUSTOMER_REQUIRED", "customerUuid 為必填");
        }

        log.info("[UseCase] createImport cmd={}", cmd);

        // 進口路由固定 IMPORT，不給傳其他 type 來混亂
        if (cmd.getCustomerUuid() == null)
            throw new BusinessException("TOM_CUSTOMER_EMPTY", "customerUuid 不得為空");
        if (StringUtils.isBlank(cmd.getDeliveryOrderLocation()))
            throw new BusinessException("TOM_IMPORT_DELIVERY_LOC_EMPTY", "deliveryOrderLocation 不得為空");

        // Domain 建立（Aggregate Root）
        CreateImportOrderSpec spec = importOrderMapper.toSpec(cmd);
        Order aggregate = domainService.createImport(spec);

        log.info(
                "[UseCase] createImport success orderNo={}, uuid={}",
                aggregate.getOrderNo(), aggregate.getUuid()
        );

        // Aggregate → Response DTO
        return importOrderMapper.toResp(aggregate);
    }
}
