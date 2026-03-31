package com.elocate.elocate.repository;

import com.elocate.elocate.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    org.springframework.data.domain.Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId,
            org.springframework.data.domain.Pageable pageable);

    List<WalletTransaction> findByUserIdAndCreatedAtBetween(UUID userId, java.time.LocalDateTime start,
            java.time.LocalDateTime end);

    @Query("SELECT MAX(t.points) FROM WalletTransaction t WHERE t.userId = :userId AND t.transactionType = 'CREDIT'")
    Optional<BigDecimal> findHighestTransactionByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT t.userId) FROM WalletTransaction t")
    long countDistinctUsers();

    /** Returns rank of user by total CREDIT points (1 = highest earner) */
    @Query(value = """
        SELECT rank FROM (
            SELECT user_id, RANK() OVER (ORDER BY SUM(points) DESC) AS rank
            FROM wallet_transaction
            WHERE transaction_type = 'CREDIT'
            GROUP BY user_id
        ) ranked
        WHERE user_id = :userId
        """, nativeQuery = true)
    Optional<Integer> findUserRank(@Param("userId") UUID userId);
}
