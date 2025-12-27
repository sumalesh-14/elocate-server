package com.elocate.elocate.repository;

import com.elocate.elocate.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    
    /**
     * Find the default address for a user
     * 
     * @param userId User ID
     * @param isDefault Should be true to find default address
     * @return Optional containing the default address if found
     */
    Optional<UserAddress> findByUserIdAndIsDefault(UUID userId, Boolean isDefault);
}
