package com.xk.truck.adm.domain.service;

import com.xk.truck.adm.domain.model.SysParam;
import com.xk.truck.adm.domain.repository.SysParamRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysParamService {
    private final SysParamRepository repo;

    @Transactional public SysParam upsert(SysParam req) {
        return repo.findByKey(req.getKey()).map(e -> {
            e.setValue(req.getValue());
            e.setDescription(req.getDescription());
            return repo.save(e);
        }).orElseGet(() -> repo.save(req));
    }

    public String getValue(String key, String def) {
        return repo.findByKey(key).map(SysParam::getValue).orElse(def);
    }
}
