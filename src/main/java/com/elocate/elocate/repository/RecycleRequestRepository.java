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

        // All requests for a user (no filters)
        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.userId = :userId " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByUserId(@Param("userId") UUID userId);

        // Filter by status only
        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.userId = :userId " +
                        "AND r.status = :status " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByUserIdAndStatus(
                        @Param("userId") UUID userId,
                        @Param("status") RecycleStatus status);

        // Filter by searchTerm only
        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.userId = :userId " +
                        "AND (LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByUserIdAndSearchTerm(
                        @Param("userId") UUID userId,
                        @Param("searchTerm") String searchTerm);

        // Filter by both status and searchTerm
        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.userId = :userId " +
                        "AND r.status = :status " +
                        "AND (LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByUserIdAndStatusAndSearchTerm(
                        @Param("userId") UUID userId,
                        @Param("status") RecycleStatus status,
                        @Param("searchTerm") String searchTerm);

        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.recyclingFacility.id = :facilityId " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByFacilityId(@Param("facilityId") UUID facilityId);

        // Legacy methods used by PartnerManagementService
        Long countByRecyclingFacility(com.elocate.elocate.model.RecyclingFacility recyclingFacility);

        Long countByRecyclingFacilityAndStatus(com.elocate.elocate.model.RecyclingFacility recyclingFacility,
                        String status);

        // Find requests with SLA breach (driver assigned but not picked up within time
        // limit)
        @Query("SELECT r FROM RecycleRequest r " +
                        "WHERE r.fulfillmentStatus IN :statuses " +
                        "AND r.updatedAt < :deadline " +
                        "AND r.assignedDriverId IS NOT NULL")
        List<RecycleRequest> findByFulfillmentStatusInAndUpdatedAtBefore(
                        @Param("statuses") List<com.elocate.elocate.model.enums.FulfillmentStatus> statuses,
                        @Param("deadline") java.time.LocalDateTime deadline);

        // --- Admin/Global Queries ---

        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findAllWithDetails();

        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.status = :status " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByStatusWithDetails(@Param("status") RecycleStatus status);

        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE (LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(r.requestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findBySearchTermWithDetails(@Param("searchTerm") String searchTerm);

        @Query("SELECT r FROM RecycleRequest r " +
                        "JOIN FETCH r.deviceModel dm " +
                        "JOIN FETCH dm.brand b " +
                        "JOIN FETCH dm.category c " +
                        "LEFT JOIN FETCH r.pickupAddress pa " +
                        "WHERE r.status = :status " +
                        "AND (LOWER(dm.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(r.requestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "ORDER BY r.createdAt DESC")
        List<RecycleRequest> findByStatusAndSearchTermWithDetails(
                        @Param("status") RecycleStatus status,
                        @Param("searchTerm") String searchTerm);
}
