package com.xk.truck.tom.domain.service;

import com.xk.base.exception.BusinessException;
import com.xk.truck.tom.domain.model.TomOrder;
import org.springframework.stereotype.Component;

@Component
public class TomOrderPolicy {

    public void validateCreate(TomOrder tom) {
        // MVP: 留白即可；企業版可以加：
        // - customer 名稱格式
        // - pickup/delivery 長度/合法性
        if (tom.getOrderNo().length() > 32) {
            throw new BusinessException("ORDER_NO_TOO_LONG", "orderNo 長度不可超過 32");
        }
    }
}
