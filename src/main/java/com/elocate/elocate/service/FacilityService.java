package com.elocate.elocate.service;

import com.elocate.elocate.dto.FacilityResponse;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityService {
    
    private final RecyclingFacilityRepository facilityRepository;
    
    public List<FacilityResponse> getAllFacilities() {
        log.info("Fetching all facilities");
        return facilityRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    private FacilityResponse mapToResponse(RecyclingFacility facility) {
        return FacilityResponse.builder()
                .address(facility.getAddress())
                .distance(0.0)
                .name(facility.getName())
                .capacity(facility.getCapacity() != null ? facility.getCapacity() : 0)
                .lon(facility.getLongitude().doubleValue())
                .lat(facility.getLatitude().doubleValue())
                .contact(facility.getContactNumber())
                .time(facility.getOperatingHours())
                .verified(facility.getIsVerified())
                .build();
    }
    
    public Optional<RecyclingFacility> getFacilityById(UUID id) {
        return facilityRepository.findById(id);
    }
    
    public RecyclingFacility createFacility(RecyclingFacility facility) {
        return facilityRepository.save(facility);
    }
    
    public RecyclingFacility updateFacility(UUID id, RecyclingFacility facility) {
        facility.setId(id);
        return facilityRepository.save(facility);
    }
    
    public void deleteFacility(UUID id) {
        facilityRepository.deleteById(id);
    }
}