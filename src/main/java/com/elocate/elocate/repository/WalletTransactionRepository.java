package com.elocate.elocate.repository;

import com.elocate.elocate.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    
    // Find transactions by user ID
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
