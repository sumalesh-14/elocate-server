package com.elocate.elocate.repository;

import com.elocate.elocate.model.RecycleStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecycleStatusHistoryRepository extends JpaRepository<RecycleStatusHistory, UUID> {
    
    /**
     * Find all status history entries for a recycle request, ordered by change time descending
     * 
     * @param recycleRequestId Recycle request ID
     * @return List of status history entries
     */
    List<RecycleStatusHistory> findByRecycleRequestIdOrderByChangedAtDesc(UUID recycleRequestId);
}
