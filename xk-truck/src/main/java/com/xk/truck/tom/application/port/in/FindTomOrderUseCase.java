package com.xk.truck.tom.application.port.in;

import com.xk.truck.tom.application.dto.qry.FindTomOrderQry;
import com.xk.truck.tom.controller.api.dto.resp.TomOrderResp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * ===============================================================
 * UseCase Class : FindTomOrderUseCase
 * Layer         :
 * Purpose       :
 * Notes         :
 * ===============================================================
 */
public interface FindTomOrderUseCase {

    TomOrderResp findById(UUID id);

    Page<TomOrderResp> pageForList(FindTomOrderQry qry, Pageable pageable);
}
