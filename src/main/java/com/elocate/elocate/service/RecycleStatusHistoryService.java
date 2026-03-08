package com.elocate.elocate.service;

import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.model.RecycleStatusHistory;
import com.elocate.elocate.model.User;
import com.elocate.elocate.repository.RecycleStatusHistoryRepository;
import com.elocate.elocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing status history tracking
 * Tracks both RecycleStatus and FulfillmentStatus changes with meaningful comments
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecycleStatusHistoryService {
    
    private final RecycleStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    
    /**
     * Record a FulfillmentStatus change in history
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
     * Record a FulfillmentStatus change in history with comments
     */
    @Transactional
    public void recordStatusChange(
            RecycleRequest request,
            FulfillmentStatus oldStatus,
            FulfillmentStatus newStatus,
            UUID changedBy,
            String comments) {
        
        String oldStatusStr = oldStatus != null ? oldStatus.name() : null;
        String newStatusStr = newStatus != null ? newStatus.name() : null;
        
        // Generate default comment if not provided
        if (comments == null || comments.isBlank()) {
            comments = generateFulfillmentComment(oldStatus, newStatus, changedBy);
        }
        
        recordStatusChangeInternal(request, oldStatusStr, newStatusStr, changedBy, comments);
    }
    
    /**
     * Record a RecycleStatus change in history
     */
    @Transactional
    public void recordRecycleStatusChange(
            RecycleRequest request,
            RecycleStatus oldStatus,
            RecycleStatus newStatus,
            UUID changedBy) {
        recordRecycleStatusChange(request, oldStatus, newStatus, changedBy, null);
    }
    
    /**
     * Record a RecycleStatus change in history with comments
     */
    @Transactional
    public void recordRecycleStatusChange(
            RecycleRequest request,
            RecycleStatus oldStatus,
            RecycleStatus newStatus,
            UUID changedBy,
            String comments) {
        
        String oldStatusStr = oldStatus != null ? oldStatus.name() : null;
        String newStatusStr = newStatus != null ? newStatus.name() : null;
        
        // Generate default comment if not provided
        if (comments == null || comments.isBlank()) {
            comments = generateRecycleStatusComment(oldStatus, newStatus, changedBy);
        }
        
        recordStatusChangeInternal(request, oldStatusStr, newStatusStr, changedBy, comments);
    }
    
    /**
     * Internal method to record status change
     */
    private void recordStatusChangeInternal(
            RecycleRequest request,
            String oldStatus,
            String newStatus,
            UUID changedBy,
            String comments) {
        
        RecycleStatusHistory history = RecycleStatusHistory.builder()
                .recycleRequest(request)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .comments(comments)
                .build();
        
        statusHistoryRepository.save(history);
        log.info("Recorded status change for request {}: {} -> {} by {} - {}", 
                request.getId(), oldStatus, newStatus, changedBy, comments);
    }
    
    /**
     * Generate meaningful comment for FulfillmentStatus changes
     */
    private String generateFulfillmentComment(FulfillmentStatus oldStatus, FulfillmentStatus newStatus, UUID changedBy) {
        if (newStatus == null) {
            return "Status updated";
        }

        String userName = getUserName(changedBy);
        
        switch (newStatus) {
            case PICKUP_REQUESTED:
                return "Pickup request created by " + userName;
            case PICKUP_ASSIGNED:
                return "Driver assigned for pickup by " + userName;
            case PICKUP_COMPLETED:
                return "Device successfully picked up by driver " + userName;
            case PICKUP_FAILED:
                return "Pickup attempt failed - reported by driver " + userName;
            case DROP_PENDING:
                return "Awaiting citizen to drop off device at facility";
            case DROPPED_AT_FACILITY:
                return "Device received at facility - confirmed by " + userName;
            case DROP_VERIFIED:
                return "Drop-off verified and accepted by " + userName;
            case REJECTED:
                return "Request rejected by " + userName;
            default:
                return "Fulfillment status updated to " + newStatus.name() + " by " + userName;
        }
    }
    
    /**
     * Generate meaningful comment for RecycleStatus changes
     */
    private String generateRecycleStatusComment(RecycleStatus oldStatus, RecycleStatus newStatus, UUID changedBy) {
        if (newStatus == null) {
            return "Status updated";
        }

        String userName = getUserName(changedBy);
        
        switch (newStatus) {
            case CREATED:
                return "Recycle request created by citizen " + userName;
            case APPROVED:
                return "Request approved by intermediary " + userName + " - Ready for fulfillment";
            case VERIFIED:
                return "Device condition verified at facility by " + userName;
            case RECYCLED:
                return "Device successfully recycled and processed by " + userName + " - Wallet credited";
            case REJECTED:
                return "Request rejected by " + userName;
            case CANCELLED:
                return "Request cancelled by " + userName;
            case LOCKED:
                return "Request locked due to fulfillment issue - requires review by " + userName;
            default:
                return "Request status updated to " + newStatus.name() + " by " + userName;
        }
    }
    
    /**
     * Get user name for comments
     */
    private String getUserName(UUID userId) {
        if (userId == null) {
            return "System";
        }
        
        return userRepository.findById(userId)
                .map(user -> {
                    String name = user.getFullName();
                    if (name == null || name.isBlank()) {
                        return user.getEmail();
                    }
                    return name;
                })
                .orElse("User-" + userId.toString().substring(0, 8));
    }
    
    /**
     * Get status history for a recycle request
     */
    @Transactional(readOnly = true)
    public List<RecycleStatusHistory> getStatusHistory(UUID recycleRequestId) {
        return statusHistoryRepository.findByRecycleRequestIdOrderByChangedAtDesc(recycleRequestId);
    }
}
