package com.xk.base.infra.sequence.repository;

import com.xk.base.infra.sequence.entity.SequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceJpaRepository extends JpaRepository<SequenceEntity, String> {
}
