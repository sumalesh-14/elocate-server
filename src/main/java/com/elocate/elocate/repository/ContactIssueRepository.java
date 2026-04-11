package com.elocate.elocate.repository;

import com.elocate.elocate.model.ContactIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactIssueRepository extends JpaRepository<ContactIssue, UUID> {

    Page<ContactIssue> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<ContactIssue> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT c FROM ContactIssue c WHERE " +
            "(:status IS NULL OR c.status = :status) AND (" +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.message) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ContactIssue> searchIssues(@Param("search") String search, @Param("status") String status, Pageable pageable);

    long countByStatus(String status);
}
