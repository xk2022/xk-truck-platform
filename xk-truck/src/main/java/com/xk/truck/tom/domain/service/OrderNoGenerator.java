// com.xk.truck.tom.domain.service.OrderNoGenerator
package com.xk.truck.tom.domain.service;

import com.xk.truck.tom.domain.model.OrderType;
import java.time.ZonedDateTime;

public interface OrderNoGenerator {
    String next(OrderType orderType, ZonedDateTime now);
}
