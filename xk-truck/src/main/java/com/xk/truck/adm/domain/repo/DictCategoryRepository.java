package com.xk.truck.adm.domain.repo;

import com.xk.truck.adm.domain.model.DictCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DictCategoryRepository extends JpaRepository<DictCategory, UUID> {
    Optional<DictCategory> findByCode(String code);
}

