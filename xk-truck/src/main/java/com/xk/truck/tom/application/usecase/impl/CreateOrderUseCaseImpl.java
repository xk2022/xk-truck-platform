package com.xk.truck.tom.application.usecase.impl;

import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.CreateOrderUseCase;
import com.xk.truck.tom.application.usecase.cmd.*;
import com.xk.truck.tom.application.usecase.dto.OrderResp;
import com.xk.truck.tom.domain.service.OrderDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderDomainService domainService;
    private final OrderMapper mapper;

    @Override
    public OrderResp execute(CreateOrderReq req) {
//        // 最基本防呆（更嚴謹可用 validator）
//        if (req == null) {
//            throw new BusinessException("TOM_ORDER_REQ_EMPTY", "建立訂單請求不得為空");
//        }
//        if (req.getOrderType() == null) {
//            throw new BusinessException("TOM_ORDER_TYPE_EMPTY", "orderType 不得為空");
//        }
//
//        log.info("[UseCase] create order start, orderType={}", req.getOrderType());
//
//        CreateOrderCmd cmd = mapper.toCreateCmd(req);
//        Order created = domainService.create(cmd);
//
//        log.info("[UseCase] create order done, orderNo={}", created.getOrderNo());
//        return mapper.toResponseDto(created);
        return null;
    }
}
