package com.elocate.elocate.service;

import com.elocate.elocate.dto.MetalRateRequest;
import com.elocate.elocate.dto.MetalRateResponse;
import com.elocate.elocate.exception.MetalRateNotFoundException;
import com.elocate.elocate.model.MetalRate;
import com.elocate.elocate.model.MetalType;
import com.elocate.elocate.repository.MetalRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetalRateService {
    
    private final MetalRateRepository metalRateRepository;
    
    /**
     * Get all metal rates with optional filters
     */
    @Transactional(readOnly = true)
    public List<MetalRateResponse> getAllMetalRates(MetalType metalType, Boolean isActive) {
        log.info("Fetching metal rates - metalType: {}, isActive: {}", metalType, isActive);
        
        List<MetalRate> rates;
        
        if (metalType != null && isActive != null) {
            rates = metalRateRepository.findByMetalTypeAndIsActive(metalType, isActive);
        } else if (metalType != null) {
            rates = metalRateRepository.findByMetalTypeOrderByEffectiveFromDesc(metalType);
        } else if (isActive != null) {
            rates = metalRateRepository.findByIsActiveOrderByEffectiveFromDesc(isActive);
        } else {
            rates = metalRateRepository.findAll();
        }
        
        return rates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get metal rate by ID
     */
    @Transactional(readOnly = true)
    public MetalRateResponse getMetalRateById(UUID id) {
        log.info("Fetching metal rate by id: {}", id);
        MetalRate rate = metalRateRepository.findById(id)
                .orElseThrow(() -> new MetalRateNotFoundException(id));
        return mapToResponse(rate);
    }
    
    /**
     * Create new metal rate
     */
    @Transactional
    public MetalRateResponse createMetalRate(MetalRateRequest request) {
        log.info("Creating new metal rate for type: {}", request.getMetalType());
        
        MetalRate rate = MetalRate.builder()
                .metalType(request.getMetalType())
                .unit(request.getUnit())
                .pointsPerUnit(request.getPointsPerUnit())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        MetalRate saved = metalRateRepository.save(rate);
        log.info("Metal rate created successfully with id: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update existing metal rate
     */
    @Transactional
    public MetalRateResponse updateMetalRate(UUID id, MetalRateRequest request) {
        log.info("Updating metal rate with id: {}", id);
        
        MetalRate rate = metalRateRepository.findById(id)
                .orElseThrow(() -> new MetalRateNotFoundException(id));
        
        rate.setMetalType(request.getMetalType());
        rate.setUnit(request.getUnit());
        rate.setPointsPerUnit(request.getPointsPerUnit());
        rate.setEffectiveFrom(request.getEffectiveFrom());
        rate.setEffectiveTo(request.getEffectiveTo());
        rate.setIsActive(request.getIsActive() != null ? request.getIsActive() : rate.getIsActive());
        
        MetalRate updated = metalRateRepository.save(rate);
        log.info("Metal rate updated successfully");
        
        return mapToResponse(updated);
    }
    
    /**
     * Map entity to response DTO
     */
    private MetalRateResponse mapToResponse(MetalRate rate) {
        return MetalRateResponse.builder()
                .id(rate.getId())
                .metalType(rate.getMetalType())
                .unit(rate.getUnit())
                .pointsPerUnit(rate.getPointsPerUnit())
                .effectiveFrom(rate.getEffectiveFrom())
                .effectiveTo(rate.getEffectiveTo())
                .isActive(rate.getIsActive())
                .createdAt(rate.getCreatedAt())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}
