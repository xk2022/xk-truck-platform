package com.xk.truck.tom.domain.service.impl;

import com.xk.base.exception.BusinessException;
import com.xk.truck.tom.application.mapper.ImportOrderMapper;
import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.cmd.FindOrderCmd;
import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.domain.model.*;
import com.xk.truck.tom.domain.repository.OrderRepository;
import com.xk.truck.tom.domain.service.OrderDomainService;
import com.xk.truck.tom.domain.service.OrderNoGenerator;

import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * OrderDomainServiceImpl
 * - 訂單 Service 實作
 * - 負責處理 OrderEntity 的核心邏輯
 * <p>
 * Domain Service → Repository → Persistence
 *
 * @author yuan Created on 2025/12/17.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderDomainServiceImpl implements OrderDomainService {

    private final OrderRepository repository;     // domain repo interface
    private final OrderMapper orderMapper;
    private final ImportOrderMapper importOrderMapper;
    private final OrderNoGenerator orderNoGenerator;   // domain policy

    @Override
    public Order createImport(CreateImportOrderSpec spec) {
        if (spec == null) throw new BusinessException("TOM_CREATE_REQ_EMPTY", "建立訂單請求不得為空");
        if (spec.getCustomerUuid() == null) throw new BusinessException("TOM_CUSTOMER_EMPTY", "customerUuid 不得為空");
//        if (spec.getRoute() == null) throw new BusinessException("TOM_ROUTE_EMPTY", "route 不得為空");
//        if (StringUtils.isBlank(spec.getRoute().getPickupAddress())) throw new BusinessException("TOM_PICKUP_EMPTY", "pickupAddress 不得為空");
//        if (StringUtils.isBlank(spec.getRoute().getDeliveryAddress())) throw new BusinessException("TOM_DELIVERY_EMPTY", "deliveryAddress 不得為空");
//        if (spec.getImportDetail() == null) throw new BusinessException("TOM_IMPORT_DETAIL_EMPTY", "importDetail 不得為空");
//        if (StringUtils.isBlank(spec.getImportDetail().getDeliveryOrderLocation())) throw new BusinessException("TOM_IMPORT_LOCATION_EMPTY", "deliveryOrderLocation 不得為空");
//        if (StringUtils.isBlank(spec.getImportDetail().getImportDeclNo())) throw new BusinessException("TOM_IMPORT_DECL_EMPTY", "importDeclNo 不得為空");

        // 1) build base order
        String orderNo = orderNoGenerator.next(OrderType.IMPORT, ZonedDateTime.now());
        if (repository.existsByOrderNo(orderNo)) {
            throw new BusinessException("TOM_ORDER_NO_DUP", "orderNo duplicated: " + orderNo);
        }

        Order order = importOrderMapper.toAggregate(spec);
        order.setOrderNo(orderNo);
        order.setOrderType(OrderType.IMPORT);
        order.setOrderStatus(OrderStatus.CREATED);

        CustomerSnapshot customerSnapshot = new CustomerSnapshot();
        customerSnapshot.setCustomerUuid(spec.getCustomerUuid());
        order.setCustomer(customerSnapshot);

        // 2) attach import detail
        ImportOrderDetail importOrderDetail = importOrderMapper.toImportOrderDetail(spec);
        order.setImportDetail(importOrderDetail);

        // 這裡用 exists 防呆 OK，但更推薦 DB unique + retry 生成
        if (repository.existsByOrderNo(order.getOrderNo())) {
            throw new BusinessException("TOM_ORDER_NO_DUP", "orderNo duplicated: " + order.getOrderNo());
        }

        // 3) save (cascade)
        Order saved = repository.save(order);
        log.info("[Domain] createImport ok orderNo={}, uuid={}", saved.getOrderNo(), saved.getUuid());
        return saved;
    }

    @Override
    public Page<OrderListItemProjection> pageForListProjection(FindOrderQry q, Pageable pageable) {
        return repository.pageForListProjection(q, pageable);
    }

    @Override
    public Order getOrderDetail(UUID id) {
        return repository.getOrderDetail(id);
    }
}
