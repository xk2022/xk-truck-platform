package com.xk.truck.tom.application.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.tom.application.usecase.cmd.FindOrderCmd;
import com.xk.truck.tom.application.usecase.dto.OrderDetailResp;
import com.xk.truck.tom.application.usecase.dto.OrderResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.controller.api.dto.OrderQuery;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.infra.persistence.entity.OrderEntity;
import org.springframework.stereotype.Component;

/**
 * OrderMapper
 * - 負責 Order 的 Dto ↔ Entity / Bo / Cmd 轉換
 *
 * @author yuan Created on 2025/12/17.
 */
@Component
public class OrderMapper {

    public OrderResp toResp(Order aggregate) {
        return XkBeanUtils.copyProperties(aggregate, OrderResp::new);
    }

    public Order toAggregate(FindOrderCmd cmd) {
        return XkBeanUtils.copyProperties(cmd, Order::new);
    }

    public FindOrderQry toQry(OrderQuery query) {
        return XkBeanUtils.copyProperties(query, FindOrderQry::new);
    }

    public OrderDetailResp toDetailResp(Order aggregate) {
        return XkBeanUtils.copyProperties(aggregate, OrderDetailResp::new);
    }

//    public OrderEntity toEntity(CreateOrderReq input) {
//        return XkBeanUtils.copyProperties(input, OrderEntity::new);
//    }
//
//    public CreateOrderCmd toCreateCmd(CreateOrderReq input) {
//        return XkBeanUtils.copyProperties(input, CreateOrderCmd::new);
//    }
//
//    public OrderResp toResponseDto(Order input) {
//        return XkBeanUtils.copyProperties(input, OrderResp::new);
//    }
//
//    public CreateImportOrderCmd toImportCmd(CreateImportOrderReq input) {
//        return XkBeanUtils.copyProperties(input, CreateImportOrderCmd::new);
//    }
//
//    public OrderResp toResp(Order input) {
//        return XkBeanUtils.copyProperties(input, OrderResp::new);
//    }
//
//    public OrderEntity toEntity(Order input) {
//        OrderEntity entity = XkBeanUtils.copyProperties(input, OrderEntity::new);
//        entity.setCustomerUuid(input.getCustomer().getCustomerUuid());
//
//        if (input.getOrderType() == OrderType.IMPORT && input.getImportDetail() != null) {
//            ImportOrderEntity importOrderEntity = XkBeanUtils.copyProperties(input, ImportOrderEntity::new);
//            entity.attachImportDetail(importOrderEntity); // setOrder + uuid
//        }
//
//        return entity;
//    }
//
//    public Order toDomain(OrderEntity input) {
//        // 這裡先回「你 Create 會用到」的欄位即可（之後 list/detail 再補）
//        return XkBeanUtils.copyProperties(input, Order::new);
//    }
//
//    public Order cmdToAggregate(CreateImportOrderCmd input) {
//        return XkBeanUtils.copyProperties(input, Order::new);
//    }
//
//    public ImportOrderDetail cmdToImportOrderDetail(CreateImportOrderCmd input) {
//        return XkBeanUtils.copyProperties(input, ImportOrderDetail::new);
//    }
//
//    public OrderEntity queryToEntity(OrderQuery input) {
//        return XkBeanUtils.copyProperties(input, OrderEntity::new);
//    }
}
