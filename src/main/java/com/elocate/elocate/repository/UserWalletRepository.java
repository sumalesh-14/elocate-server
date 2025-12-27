package com.elocate.elocate.repository;

import com.elocate.elocate.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID> {
    
    // Find wallet by user ID
    Optional<UserWallet> findByUserId(UUID userId);
}
