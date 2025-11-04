package com.xk.truck.upms.domain.dao.repository;

import com.xk.truck.upms.domain.model.po.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: PermissionRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 Permission 實體的 CRUD 與常用查詢
 * Notes    :
 * - 權限以 code 唯一識別（如：USER_VIEW、USER_EDIT）
 * ===============================================================
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * 是否已存在相同權限代碼
     */
    boolean existsByCode(String code);

    /**
     * 以代碼查詢權限
     */
    Optional<Permission> findByCode(String code);

    Set<Permission> findByCodeIn(Set<String> permissionCodes);
}
