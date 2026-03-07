package com.elocate.elocate.repository;

import com.elocate.elocate.model.PricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PricingHistoryRepository extends JpaRepository<PricingHistory, UUID> {
    List<PricingHistory> findByRecycleRequestIdOrderByCreatedAtDesc(UUID recycleRequestId);
    
    List<PricingHistory> findByChangedByOrderByCreatedAtDesc(UUID changedBy);
    
    List<PricingHistory> findByFieldNameOrderByCreatedAtDesc(String fieldName);
}
