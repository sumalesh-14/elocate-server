package com.elocate.elocate.controller;

import com.elocate.elocate.dto.DriverRequestDto;
import com.elocate.elocate.dto.DriverResponseDto;
import com.elocate.elocate.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverResponseDto> createDriver(@Valid @RequestBody DriverRequestDto request) {
        log.info("POST /api/v1/drivers - Email: {}", request.getEmail());
        DriverResponseDto response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DriverResponseDto>> getAllDrivers(
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        log.info("GET /api/v1/drivers - availability: {}, search: {}, page: {}, size: {}", availability, search, page,
                size);
        Page<DriverResponseDto> responses = driverService.getAllDrivers(availability, search, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDto> getDriverById(@PathVariable UUID id) {
        log.info("GET /api/v1/drivers/{}", id);
        DriverResponseDto response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponseDto> updateDriver(
            @PathVariable UUID id,
            @Valid @RequestBody DriverRequestDto request) {
        log.info("PUT /api/v1/drivers/{} - Email: {}", id, request.getEmail());
        DriverResponseDto response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        log.info("DELETE /api/v1/drivers/{}", id);
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
