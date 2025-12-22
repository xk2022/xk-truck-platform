package com.xk.truck.tom.application.usecase.impl;

import com.xk.base.exception.BusinessException;
import com.xk.truck.tom.application.mapper.OrderMapper;
import com.xk.truck.tom.application.usecase.FindOrderUseCase;
import com.xk.truck.tom.application.usecase.dto.OrderListItemResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.domain.service.OrderDomainService;

import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindOrderUseCaseImpl implements FindOrderUseCase {

    private final OrderDomainService domainService;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderListItemResp> pageForList(FindOrderQry q, Pageable pageable) {
        if (pageable.getPageNumber() < 0) throw new BusinessException("TOM_PAGE_INVALID", "page 需 >= 0");
        if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 200)
            throw new BusinessException("TOM_SIZE_INVALID", "size 需在 1~200");

        Page<OrderListItemProjection> rows = domainService.pageForListProjection(q, pageable);

        // Projection -> Resp：純 mapping，不碰 entity、0 lazy、0 N+1
        return rows.map(r -> {
            OrderListItemResp resp = new OrderListItemResp();

            resp.setId(String.valueOf(r.getUuid()));
            resp.setOrderNo(r.getOrderNo());
            resp.setOrderType(r.getOrderType());
            resp.setOrderStatus(r.getOrderStatus());
            resp.setCustomerUuid(r.getCustomerUuid());

            resp.setPickupAddress(r.getPickupAddress());
            resp.setDeliveryAddress(r.getDeliveryAddress());

            resp.setShippingCompany(r.getShippingCompany());
            resp.setVesselVoyage(r.getVesselVoyage());

            resp.setContainerNo(r.getContainerNo());
            resp.setContainerType(r.getContainerType());

            resp.setCreatedTime(r.getCreatedTime());

            resp.setImportDeclNo(r.getImportDeclNo());
            resp.setImportDeliveryOrderLocation(r.getDeliveryOrderLocation());
            resp.setBlNo(r.getBlNo());

            resp.setImportDeclNo(r.getExportDeclNo());
            resp.setBookingNo(r.getBookingNo());
            resp.setSoNo(r.getSoNo());

            return resp;
        });
    }
}
