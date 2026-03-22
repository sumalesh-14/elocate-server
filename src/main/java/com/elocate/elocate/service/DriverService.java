package com.elocate.elocate.service;

import com.elocate.elocate.dto.DriverRequestDto;
import com.elocate.elocate.dto.DriverResponseDto;
import com.elocate.elocate.model.Driver;
import com.elocate.elocate.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponseDto createDriver(DriverRequestDto request) {
        log.info("Creating new driver with email: {}", request.getEmail());

        if (driverRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Driver with email " + request.getEmail() + " already exists.");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .vehicleNumber(request.getVehicleNumber())
                .availability(request.getAvailability() != null ? request.getAvailability() : "AVAILABLE")
                .vehicleType(request.getVehicleType() != null ? request.getVehicleType() : "VAN")
                .facilityId(request.getFacilityId())
                .build();

        Driver saved = driverRepository.save(driver);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DriverResponseDto> getAllDrivers(UUID facilityId, String availability, String searchTerm, int page, int size) {
        log.info("Fetching drivers - facilityId: {}, availability: {}, search: {}, page: {}, size: {}",
                facilityId, availability, searchTerm, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Driver> driversPage;

        boolean hasFacility = facilityId != null;
        boolean hasAvailability = availability != null && !availability.isBlank()
                && !availability.equalsIgnoreCase("All");
        boolean hasSearch = searchTerm != null && !searchTerm.isBlank();

        if (hasFacility) {
            if (hasAvailability && hasSearch) {
                driversPage = driverRepository.findByFacilityIdAndAvailabilityAndSearchTerm(facilityId, availability, searchTerm, pageable);
            } else if (hasAvailability) {
                driversPage = driverRepository.findByFacilityIdAndAvailabilityIgnoreCase(facilityId, availability, pageable);
            } else if (hasSearch) {
                driversPage = driverRepository.searchDriversByFacility(facilityId, searchTerm, pageable);
            } else {
                driversPage = driverRepository.findByFacilityId(facilityId, pageable);
            }
        } else {
            if (hasAvailability && hasSearch) {
                driversPage = driverRepository.findByAvailabilityAndSearchTerm(availability, searchTerm, pageable);
            } else if (hasAvailability) {
                driversPage = driverRepository.findByAvailabilityIgnoreCase(availability, pageable);
            } else if (hasSearch) {
                driversPage = driverRepository.searchDrivers(searchTerm, pageable);
            } else {
                driversPage = driverRepository.findAll(pageable);
            }
        }

        return driversPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DriverResponseDto getDriverById(UUID id) {
        log.info("Fetching driver by id: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));
        return mapToResponse(driver);
    }

    @Transactional
    public DriverResponseDto updateDriver(UUID id, DriverRequestDto request) {
        log.info("Updating driver: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        if (!driver.getEmail().equals(request.getEmail())
                && driverRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Driver with email " + request.getEmail() + " already exists.");
        }

        driver.setName(request.getName());
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setVehicleNumber(request.getVehicleNumber());
        if (request.getAvailability() != null) {
            driver.setAvailability(request.getAvailability());
        }
        if (request.getVehicleType() != null) {
            driver.setVehicleType(request.getVehicleType());
        }
        if (request.getFacilityId() != null) {
            driver.setFacilityId(request.getFacilityId());
        }

        Driver updated = driverRepository.save(driver);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteDriver(UUID id) {
        log.info("Deleting driver: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));
        driverRepository.delete(driver);
    }

    private DriverResponseDto mapToResponse(Driver driver) {
        return DriverResponseDto.builder()
                .id(driver.getId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .vehicleNumber(driver.getVehicleNumber())
                .availability(driver.getAvailability())
                .vehicleType(driver.getVehicleType())
                .facilityId(driver.getFacilityId())
                .build();
    }
}
