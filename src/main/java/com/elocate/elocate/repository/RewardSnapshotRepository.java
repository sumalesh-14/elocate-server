package com.elocate.elocate.repository;

import com.elocate.elocate.model.RewardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RewardSnapshotRepository extends JpaRepository<RewardSnapshot, UUID> {
    
    // Find snapshot by recycle request ID
    Optional<RewardSnapshot> findByRecycleRequestId(UUID recycleRequestId);
}
