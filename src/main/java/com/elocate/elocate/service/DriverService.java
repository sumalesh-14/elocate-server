package com.elocate.elocate.service;

import com.elocate.elocate.dto.DriverRequestDto;
import com.elocate.elocate.dto.DriverResponseDto;
import com.elocate.elocate.model.Driver;
import com.elocate.elocate.repository.DriverRepository;
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
                .build();

        Driver saved = driverRepository.save(driver);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DriverResponseDto> getAllDrivers(String availability, String searchTerm) {
        log.info("Fetching drivers with availability: {}, search: {}", availability, searchTerm);

        List<Driver> drivers;
        boolean hasAvailability = availability != null && !availability.isBlank();
        boolean hasSearch = searchTerm != null && !searchTerm.isBlank();

        if (hasAvailability && hasSearch) {
            drivers = driverRepository.findByAvailabilityAndSearchTerm(availability, searchTerm);
        } else if (hasAvailability) {
            drivers = driverRepository.findByAvailabilityIgnoreCase(availability);
        } else if (hasSearch) {
            drivers = driverRepository.searchDrivers(searchTerm);
        } else {
            drivers = driverRepository.findAll();
        }

        return drivers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
                .build();
    }
}
