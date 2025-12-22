package com.elocate.elocate.controller;

import com.elocate.elocate.dto.FacilityResponse;
import com.elocate.elocate.model.RecyclingFacility;
import com.elocate.elocate.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<List<FacilityResponse>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
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
    public ResponseEntity<RecyclingFacility> updateFacility(@PathVariable UUID id, @RequestBody RecyclingFacility facility) {
        return ResponseEntity.ok(facilityService.updateFacility(id, facility));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable UUID id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}
