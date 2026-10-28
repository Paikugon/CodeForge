package com.codeforge.backend.user.repository;

import com.codeforge.backend.user.dto.UserListResponse;
import com.codeforge.backend.user.enums.UserRole;
import com.codeforge.backend.user.model.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from User u
            where u.role = :role
            order by u.id
            """)
    List<User> findAllByRoleForUpdate(@Param("role") UserRole role);

    @Query("""
            select new com.codeforge.backend.user.dto.UserListResponse(
                u.id, u.username, u.email, u.role, u.enabled,
                u.createdAt, u.updatedAt)
            from User u
            where (:role is null or u.role = :role)
              and (:enabled is null or u.enabled = :enabled)
            """)
    Page<UserListResponse> findSummaries(
            @Param("role") UserRole role,
            @Param("enabled") Boolean enabled,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from User u
            where u.id = :userId
            """)
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);
}