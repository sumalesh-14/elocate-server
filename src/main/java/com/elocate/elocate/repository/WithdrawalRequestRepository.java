package com.elocate.elocate.repository;

import com.elocate.elocate.model.WithdrawalRequest;
import com.elocate.elocate.model.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, UUID> {

    List<WithdrawalRequest> findByUserIdOrderByRequestedAtDesc(UUID userId);

    List<WithdrawalRequest> findByStatusOrderByRequestedAtAsc(WithdrawalStatus status);

    Optional<WithdrawalRequest> findByUserIdAndStatus(UUID userId, WithdrawalStatus status);

    boolean existsByUserIdAndStatus(UUID userId, WithdrawalStatus status);
}
