package com.xk.truck.adm.domain.repository;

import com.xk.truck.adm.domain.model.SysParam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SysParamRepository extends JpaRepository<SysParam, UUID> {
    Optional<SysParam> findByKey(String key);
}
