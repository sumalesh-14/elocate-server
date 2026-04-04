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

    @Query("SELECT MAX(t.points) FROM WalletTransaction t WHERE t.userId = :userId AND t.transactionType = 'RECYCLED'")
    Optional<BigDecimal> findHighestTransactionByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT t.userId) FROM WalletTransaction t WHERE t.transactionType = 'RECYCLED'")
    long countDistinctUsers();

    /** Returns rank of user by total RECYCLED points (1 = highest earner) */
    @Query(value = """
        SELECT rank FROM (
            SELECT user_id, RANK() OVER (ORDER BY SUM(points) DESC) AS rank
            FROM wallet_transaction
            WHERE transaction_type = 'RECYCLED'
            GROUP BY user_id
        ) ranked
        WHERE user_id = :userId
        """, nativeQuery = true)
    Optional<Integer> findUserRank(@Param("userId") UUID userId);

    /** All RECYCLED transactions for a given facility (via recycle_request → recycling_facility) */
    @Query("SELECT t FROM WalletTransaction t JOIN t.recycleRequest r WHERE r.recyclingFacility.id = :facilityId ORDER BY t.createdAt DESC")
    List<WalletTransaction> findByFacilityId(@Param("facilityId") UUID facilityId);

    /** All WITHDRAWN/REFUNDED transactions for citizens who have recycled at a given facility */
    @Query(value = """
        SELECT wt.* FROM wallet_transaction wt
        WHERE wt.transaction_type IN ('WITHDRAWN','REFUNDED')
        AND wt.user_id IN (
            SELECT DISTINCT rr.user_id FROM recycle_request rr
            WHERE rr.recycling_facility_id = :facilityId
        )
        ORDER BY wt.created_at DESC
        """, nativeQuery = true)
    List<WalletTransaction> findWithdrawalsByFacilityCitizens(@Param("facilityId") UUID facilityId);
}
