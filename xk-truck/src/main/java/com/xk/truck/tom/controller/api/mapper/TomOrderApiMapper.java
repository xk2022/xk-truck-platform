package com.xk.truck.tom.controller.api.mapper;

import com.xk.base.util.XkBeanUtils;
import com.xk.truck.tom.application.dto.TomOrderResult;
import com.xk.truck.tom.application.dto.cmd.CreateTomOrderCommand;

import com.xk.truck.tom.application.dto.qry.FindTomOrderQry;
import com.xk.truck.tom.controller.api.dto.req.CreateTomOrderReq;

import com.xk.truck.tom.controller.api.dto.req.TomOrderQuery;
import com.xk.truck.tom.controller.api.dto.resp.TomOrderResp;

import jakarta.validation.Valid;

import org.springframework.stereotype.Component;

/**
 * ===============================================================
 * Controller Class : TomOrderApiMapper
 * Layer            : Interface Adapters 入口層 (Mapper) Request 轉 commond
 * Purpose          : 提供 TOM 訂單管理 API（Create / List / Detail / Assign / Status）
 * ===============================================================
 */
@Component
public class TomOrderApiMapper {

    public TomOrderResp toResp(TomOrderResult result) {
        return XkBeanUtils.copyProperties(result, TomOrderResp::new);
    }

    public CreateTomOrderCommand toCreateCmd(@Valid CreateTomOrderReq input) {
        return XkBeanUtils.copyProperties(input, CreateTomOrderCommand::new);
    }

    public FindTomOrderQry toQry(TomOrderQuery query) {
        return XkBeanUtils.copyProperties(query, FindTomOrderQry::new);
    }
}
