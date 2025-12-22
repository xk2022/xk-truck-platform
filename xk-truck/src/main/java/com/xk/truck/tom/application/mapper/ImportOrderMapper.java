package com.xk.truck.tom.application.mapper;

import com.xk.truck.tom.application.usecase.dto.OrderResp;
import com.xk.truck.tom.controller.api.dto.CreateImportOrderReq;
import com.xk.truck.tom.domain.model.CreateImportOrderSpec;
import org.springframework.stereotype.Component;
import com.xk.base.util.XkBeanUtils;
import com.xk.truck.tom.application.usecase.cmd.CreateImportOrderCmd;
import com.xk.truck.tom.domain.model.ImportOrderDetail;
import com.xk.truck.tom.domain.model.Order;
import com.xk.truck.tom.infra.persistence.entity.OrderEntity;

/**
 * ImportOrderMapper
 * - 負責 ImportOrder 的 Dto ↔ Entity / Bo / Cmd 轉換
 *
 * @author yuan Created on 2025/12/17.
 */
@Component
public class ImportOrderMapper {

    public Order toAggregate(CreateImportOrderSpec spec) {
        return XkBeanUtils.copyProperties(spec, Order::new);
    }

    public ImportOrderDetail toImportOrderDetail(CreateImportOrderSpec spec) {
        return XkBeanUtils.copyProperties(spec, ImportOrderDetail::new);
    }

    public OrderEntity toEntity(Order aggregate) {
        return XkBeanUtils.copyProperties(aggregate, OrderEntity::new);
    }

    public Order toDomain(OrderEntity entity) {
        return XkBeanUtils.copyProperties(entity, Order::new);
    }

    public CreateImportOrderCmd toCmd(CreateImportOrderReq req) {
        return XkBeanUtils.copyProperties(req, CreateImportOrderCmd::new);
    }

    public CreateImportOrderSpec toSpec(CreateImportOrderCmd cmd) {
        return XkBeanUtils.copyProperties(cmd, CreateImportOrderSpec::new);
    }

    public OrderResp toResp(Order aggregate) {
        return XkBeanUtils.copyProperties(aggregate, OrderResp::new);
    }
}
