package com.elocate.elocate.repository;

import com.elocate.elocate.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByMobileNumber(String mobileNumber);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
    Optional<User> findByFirebaseUid(String firebaseUid);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByRoleAndIsActive(String role, Boolean isActive, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.fullName) LIKE :q OR LOWER(u.email) LIKE :q OR u.mobileNumber LIKE :q)")
    Page<User> findByRoleAndSearchTerm(@Param("role") String role, @Param("q") String q, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = :isActive AND (LOWER(u.fullName) LIKE :q OR LOWER(u.email) LIKE :q OR u.mobileNumber LIKE :q)")
    Page<User> findByRoleAndIsActiveAndSearchTerm(@Param("role") String role, @Param("isActive") Boolean isActive, @Param("q") String q, Pageable pageable);
}
