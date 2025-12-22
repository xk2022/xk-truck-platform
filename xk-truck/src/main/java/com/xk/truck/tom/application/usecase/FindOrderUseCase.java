package com.xk.truck.tom.application.usecase;

import com.xk.truck.tom.application.usecase.dto.OrderListItemResp;
import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ===============================================================
 * UseCase Class : FindOrderUseCase
 * Layer         :
 * Purpose       :
 * Notes         :
 * ===============================================================
 */
public interface FindOrderUseCase {

    Page<OrderListItemResp> pageForList(FindOrderQry q, Pageable pageable);
}
