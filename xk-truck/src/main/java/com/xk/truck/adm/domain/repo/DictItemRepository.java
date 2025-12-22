package com.xk.truck.adm.domain.repo;

import com.xk.truck.adm.domain.model.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DictItemRepository extends JpaRepository<DictItem, UUID> {
    List<DictItem> findByCategory_CodeAndEnabledIsTrueOrderBySortNoAsc(String categoryCode);

    boolean existsByCategory_IdAndCode(UUID categoryId, String code);
}
