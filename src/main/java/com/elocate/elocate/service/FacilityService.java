package com.elocate.elocate.service;

import com.elocate.elocate.dto.FacilityResponse;
import com.elocate.elocate.dto.FacilityWithDistanceProjection;
import com.elocate.elocate.dto.GetFacilities;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.repository.RecyclingFacilityRepository;
import com.elocate.elocate.utils.DistanceUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityService {
    
    private final RecyclingFacilityRepository facilityRepository;
    
    public List<FacilityResponse> getAllFacilities(@Valid GetFacilities request) {
        log.info("Fetching all facilities");
        return facilityRepository
                .findNearestFacilities(
                        request.getUserLatitute(),
                        request.getUserLongitude()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private FacilityResponse mapToResponse(FacilityWithDistanceProjection f) {
        return FacilityResponse.builder()
                .address(f.getAddress())
                .distance(f.getDistance())
                .name(f.getName())
                .capacity(f.getCapacity() != null ? f.getCapacity() : 0)
                .lon(f.getLongitude())
                .lat(f.getLatitude())
                .contact(f.getContactNumber())
                .time(f.getOperatingHours())
                .verified(Boolean.TRUE.equals(f.getIsVerified()))
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