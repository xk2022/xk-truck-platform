package com.xk.truck.tom.application.port.out;

import com.xk.truck.tom.domain.model.TomOrder;

public interface TomOrderAggregateRepositoryPort {
    TomOrder save(TomOrder aggregate);
}
