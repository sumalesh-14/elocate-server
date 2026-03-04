package com.elocate.elocate.repository;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecycleRequestRepository extends JpaRepository<RecycleRequest, UUID> {

    @Query("SELECT r FROM RecycleRequest r " +
            "JOIN r.deviceModel dm " +
            "JOIN dm.brand b " +
            "WHERE r.userId = :userId " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:searchTerm IS NULL OR LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY r.createdAt DESC")
    List<RecycleRequest> findByUserIdWithFilters(
            @Param("userId") UUID userId,
            @Param("status") RecycleStatus status,
            @Param("searchTerm") String searchTerm);

    // Legacy methods used by PartnerManagementService
    Long countByRecyclingFacility(com.elocate.elocate.model.RecyclingFacility recyclingFacility);

    Long countByRecyclingFacilityAndStatus(com.elocate.elocate.model.RecyclingFacility recyclingFacility,
            String status);
}
