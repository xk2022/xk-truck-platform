package com.xk.truck.tom.application.usecase.impl;

import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.GetOrderDetailUseCase;

import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.domain.service.OrderDomainService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderDetailUseCaseImpl implements GetOrderDetailUseCase {

    private final OrderDomainService domainService;
    private final OrderMapper orderMapper;

    @Override
    public OrderDetailResp findById(UUID id) {
        Order agg = domainService.getOrderDetail(id);
        return orderMapper.toDetailResp(agg);
    }
}
