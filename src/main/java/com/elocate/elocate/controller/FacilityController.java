package com.elocate.elocate.controller;

import com.elocate.elocate.dto.FacilityResponse;
import com.elocate.elocate.dto.GetFacilities;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public ResponseEntity<Page<FacilityResponse>> getAllFacilities(
            @Valid @ModelAttribute GetFacilities getFacilities) {
        if (getFacilities.getUserLatitude() == null || getFacilities.getUserLongitude() == null) {
            log.info("Fetching facilities without coordinates - returning all");
            return ResponseEntity
                    .ok(facilityService.listFacilities(getFacilities.getPage(), getFacilities.getSize(), null)
                            .map(f -> FacilityResponse.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .address(f.getAddress())
                                    .lat(f.getLatitude().doubleValue())
                                    .lon(f.getLongitude().doubleValue())
                                    .capacity(f.getCapacity() != null ? f.getCapacity() : 0)
                                    .contact(f.getContactNumber())
                                    .time(f.getOperatingHours())
                                    .verified(Boolean.TRUE.equals(f.getIsVerified()))
                                    .build()));
        }
        return ResponseEntity.ok(facilityService.getAllFacilities(getFacilities));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<RecyclingFacility>> listFacilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(facilityService.listFacilities(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecyclingFacility> getFacilityById(@PathVariable UUID id) {
        return facilityService.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecyclingFacility> createFacility(@RequestBody RecyclingFacility facility) {
        return ResponseEntity.ok(facilityService.createFacility(facility));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecyclingFacility> updateFacility(@PathVariable UUID id,
            @RequestBody RecyclingFacility facility) {
        return ResponseEntity.ok(facilityService.updateFacility(id, facility));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable UUID id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
