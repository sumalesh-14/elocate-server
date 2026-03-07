package com.elocate.elocate.repository;

import com.elocate.elocate.model.FacilityReassignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacilityReassignmentRepository extends JpaRepository<FacilityReassignment, UUID> {
    List<FacilityReassignment> findByRecycleRequestIdOrderByCreatedAtDesc(UUID recycleRequestId);
    
    List<FacilityReassignment> findByOldFacilityIdOrderByCreatedAtDesc(UUID oldFacilityId);
    
    List<FacilityReassignment> findByNewFacilityIdOrderByCreatedAtDesc(UUID newFacilityId);
}
