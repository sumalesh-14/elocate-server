package com.elocate.elocate.repository;

import com.elocate.elocate.model.DeviceConditionFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceConditionFactorRepository extends JpaRepository<DeviceConditionFactor, UUID> {
    
    // Find by condition code
    Optional<DeviceConditionFactor> findByConditionCodeAndIsActive(String conditionCode, Boolean isActive);
}
