package com.elocate.elocate.repository;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecyclingFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecycleRequestRepository extends JpaRepository<RecycleRequest, UUID> {
    
    // Find by user ID (would need user field added to RecycleRequest model)
    // List<RecycleRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    // Find by status
    List<RecycleRequest> findByStatusOrderByCreatedAtDesc(String status);
    
    // Count by recycling facility
    Long countByRecyclingFacility(RecyclingFacility recyclingFacility);
    
    // Count by recycling facility and status
    Long countByRecyclingFacilityAndStatus(RecyclingFacility recyclingFacility, String status);
}
