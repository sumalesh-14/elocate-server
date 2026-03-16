package com.elocate.elocate.repository;

import com.elocate.elocate.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Query("SELECT f FROM Feedback f " +
            "WHERE f.recycleRequest.id = :recycleRequestId")
    Optional<Feedback> findByRecycleRequestId(@Param("recycleRequestId") UUID recycleRequestId);

    @Query("SELECT f FROM Feedback f " +
            "WHERE f.userId = :userId " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM Feedback f " +
            "WHERE f.recycleRequest.recyclingFacility.id = :facilityId " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findByFacilityId(@Param("facilityId") UUID facilityId);

    @Query("SELECT f FROM Feedback f " +
            "ORDER BY f.createdAt DESC")
    List<Feedback> findAllOrderByCreatedAtDesc();

    @Query("SELECT AVG(f.rating) FROM Feedback f " +
            "WHERE f.recycleRequest.recyclingFacility.id = :facilityId")
    Double getAverageRatingByFacilityId(@Param("facilityId") UUID facilityId);

    @Query("SELECT COUNT(f) FROM Feedback f " +
            "WHERE f.recycleRequest.recyclingFacility.id = :facilityId")
    Long countByFacilityId(@Param("facilityId") UUID facilityId);
}
