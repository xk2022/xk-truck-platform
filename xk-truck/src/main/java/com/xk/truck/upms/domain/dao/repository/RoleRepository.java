package com.xk.truck.upms.domain.dao.repository;

import com.xk.truck.upms.domain.model.po.Role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: RoleRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 Role 實體的 CRUD 與常用查詢
 * Notes    :
 * - 角色以 code 唯一識別（如：ADMIN、DISPATCH）
 * ===============================================================
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * 是否已存在相同角色代碼
     */
    boolean existsByCode(String code);

    /**
     * 以代碼查詢角色
     */
    Optional<Role> findByCode(String code);

    Set<Role> findByCodeIn(@NotEmpty Set<@Pattern(regexp = "^[A-Z_]+$") String> strings);
}
