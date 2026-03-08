package com.elocate.elocate.repository;

import com.elocate.elocate.model.DriverPickupToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverPickupTokenRepository extends JpaRepository<DriverPickupToken, UUID> {
    
    Optional<DriverPickupToken> findByToken(String token);
    
    Optional<DriverPickupToken> findByRecycleRequestIdAndActionType(UUID recycleRequestId, String actionType);
    
    List<DriverPickupToken> findByRecycleRequestId(UUID recycleRequestId);
    
    @Modifying
    @Query("UPDATE DriverPickupToken t SET t.isActive = false WHERE t.recycleRequestId = :requestId AND t.isActive = true")
    int invalidateTokensForRequest(@Param("requestId") UUID requestId);
}
