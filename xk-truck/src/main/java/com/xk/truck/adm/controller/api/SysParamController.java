package com.xk.truck.adm.controller.api;

import com.xk.base.web.ApiResult;
import com.xk.truck.adm.domain.model.SysParam;
import com.xk.truck.adm.domain.service.SysParamService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adm/params")
@RequiredArgsConstructor
public class SysParamController {
    private final SysParamService svc;

    @PostMapping
    public ApiResult<SysParam> upsert(@RequestBody SysParam req) {
        return ApiResult.success(svc.upsert(req), "Param upserted");
    }

    @GetMapping("/{key}")
    public ApiResult<String> get(@PathVariable String key) {
        return ApiResult.success(svc.getValue(key, null));
    }
}
