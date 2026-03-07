package com.elocate.elocate.repository;

import com.elocate.elocate.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    List<EmailLog> findByRelatedEntityIdOrderByCreatedAtDesc(UUID relatedEntityId);
    
    List<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    List<EmailLog> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    
    List<EmailLog> findByStatusOrderByCreatedAtDesc(String status);
    
    List<EmailLog> findByEmailTypeOrderByCreatedAtDesc(String emailType);
    
    List<EmailLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}
