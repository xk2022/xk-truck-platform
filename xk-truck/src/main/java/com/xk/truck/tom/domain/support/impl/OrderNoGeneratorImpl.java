package com.xk.truck.tom.domain.support.impl;

import com.xk.truck.tom.domain.model.TomOrderType;
import com.xk.truck.tom.domain.support.OrderNoGenerator;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OrderNoGeneratorImpl implements OrderNoGenerator {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String next(TomOrderType orderType, ZonedDateTime now) {
        // e.g. IMP-20251218153030-AB12
        String prefix = (orderType == TomOrderType.IMPORT) ? "IMP" : "EXP";
        String ts = now.format(F);
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return prefix + "-" + ts + "-" + rand;
    }
}
