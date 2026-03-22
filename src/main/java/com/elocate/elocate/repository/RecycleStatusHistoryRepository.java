package com.elocate.elocate.repository;

import com.elocate.elocate.model.RecycleStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecycleStatusHistoryRepository extends JpaRepository<RecycleStatusHistory, UUID> {
    
    /**
     * Find all status history entries for a recycle request, ordered by change time descending
     */
    List<RecycleStatusHistory> findByRecycleRequestIdOrderByChangedAtDesc(UUID recycleRequestId);

    /**
     * Check if a reminder was already sent today for this request by this user
     */
    @Query("SELECT COUNT(h) > 0 FROM RecycleStatusHistory h " +
           "WHERE h.recycleRequest.id = :requestId " +
           "AND h.changedBy = :userId " +
           "AND h.comments LIKE 'Reminder sent to facility%' " +
           "AND h.changedAt >= :startOfDay")
    boolean existsReminderSentTodayByUser(
            @Param("requestId") UUID requestId,
            @Param("userId") UUID userId,
            @Param("startOfDay") LocalDateTime startOfDay);
}
