package com.elocate.elocate.repository;

import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.MetalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetalRateRepository extends JpaRepository<MetalRate, UUID> {
    
    // Find all active metal rates
    List<MetalRate> findByIsActiveOrderByEffectiveFromDesc(Boolean isActive);
    
    // Find by metal type
    List<MetalRate> findByMetalTypeAndIsActive(MetalType metalType, Boolean isActive);
    
    // Find current rate for a metal type (active and effective today)
    Optional<MetalRate> findFirstByMetalTypeAndIsActiveTrueAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
            MetalType metalType,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    );
    
    // Find all rates for a specific metal type
    List<MetalRate> findByMetalTypeOrderByEffectiveFromDesc(MetalType metalType);
}
