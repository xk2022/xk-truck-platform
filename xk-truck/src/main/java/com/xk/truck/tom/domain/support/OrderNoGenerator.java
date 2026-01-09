package com.xk.truck.tom.domain.support;

import com.xk.truck.tom.domain.model.TomOrderType;
import java.time.ZonedDateTime;

public interface OrderNoGenerator {
    String next(TomOrderType orderType, ZonedDateTime now);
}
