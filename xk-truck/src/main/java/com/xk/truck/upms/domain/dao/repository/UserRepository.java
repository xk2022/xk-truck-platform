package com.xk.truck.upms.domain.dao.repository;

import com.xk.truck.upms.domain.model.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: UserRepository
 * Layer    : Domain DAO (JPA Repository)
 * Purpose  : 提供 User 實體的 CRUD 與常用查詢
 * Notes    :
 * - Spring Data JPA 自動實作常規方法
 * - 以 username 作為主要登入識別欄位（唯一）
 * ===============================================================
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 是否已存在相同使用者帳號
     */
    boolean existsByUsername(String username);

    /**
     * 以帳號查詢使用者
     */
    Optional<User> findByUsername(String username);
}
