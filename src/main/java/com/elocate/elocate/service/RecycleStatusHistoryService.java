package com.elocate.elocate.service;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecycleStatusHistory;
import com.elocate.elocate.repository.RecycleStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing status history tracking
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecycleStatusHistoryService {
    
    private final RecycleStatusHistoryRepository statusHistoryRepository;
    
    /**
     * Record a status change in history
     * 
     * @param request The recycle request
     * @param oldStatus Previous fulfillment status (null if initial creation)
     * @param newStatus New fulfillment status
     * @param changedBy User ID who made the change
     */
    @Transactional
    public void recordStatusChange(
            RecycleRequest request,
            FulfillmentStatus oldStatus,
            FulfillmentStatus newStatus,
            UUID changedBy) {
        recordStatusChange(request, oldStatus, newStatus, changedBy, null);
    }
    
    /**
     * Record a status change in history with comments
     * 
     * @param request The recycle request
     * @param oldStatus Previous fulfillment status (null if initial creation)
     * @param newStatus New fulfillment status
     * @param changedBy User ID who made the change
     * @param comments Optional comments about the status change
     */
    @Transactional
    public void recordStatusChange(
            RecycleRequest request,
            FulfillmentStatus oldStatus,
            FulfillmentStatus newStatus,
            UUID changedBy,
            String comments) {
        
        RecycleStatusHistory history = RecycleStatusHistory.builder()
                .recycleRequest(request)
                .oldStatus(oldStatus != null ? oldStatus.name() : null)
                .newStatus(newStatus.name())
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .comments(comments)
                .build();
        
        statusHistoryRepository.save(history);
        log.info("Recorded status change for request {}: {} -> {} (comments: {})", 
                request.getId(), oldStatus, newStatus, comments != null ? comments : "none");
    }
    
    /**
     * Get status history for a recycle request
     * 
     * @param recycleRequestId Recycle request ID
     * @return List of status history entries, ordered by change time descending
     */
    @Transactional(readOnly = true)
    public List<RecycleStatusHistory> getStatusHistory(UUID recycleRequestId) {
        return statusHistoryRepository.findByRecycleRequestIdOrderByChangedAtDesc(recycleRequestId);
    }
}
