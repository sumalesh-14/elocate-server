package com.elocate.elocate.service;

import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.User;
import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.repository.RecycleRequestRepository;
import com.elocate.elocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to monitor driver SLA and detect breaches
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DriverSLAMonitorService {

    private final RecycleRequestRepository recycleRequestRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.driver.sla-hours:24}")
    private int slaHours;

    @Value("${app.driver.sla-monitoring-enabled:true}")
    private boolean slaMonitoringEnabled;

    /**
     * Check for SLA breaches every hour
     * Runs at minute 0 of every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkSLABreaches() {
        if (!slaMonitoringEnabled) {
            log.debug("SLA monitoring is disabled");
            return;
        }

        log.info("Starting SLA breach check for driver assignments");

        LocalDateTime slaDeadline = LocalDateTime.now().minusHours(slaHours);

        // Find requests assigned to drivers but not picked up within SLA
        List<RecycleRequest> breachedRequests = recycleRequestRepository
                .findByFulfillmentStatusInAndUpdatedAtBefore(
                        List.of(FulfillmentStatus.PICKUP_ASSIGNED),
                        slaDeadline
                );

        if (breachedRequests.isEmpty()) {
            log.info("No SLA breaches found");
            return;
        }

        log.warn("Found {} requests with SLA breach (>{} hours)", breachedRequests.size(), slaHours);

        for (RecycleRequest request : breachedRequests) {
            handleSLABreach(request);
        }
    }

    /**
     * Handle SLA breach for a request
     */
    private void handleSLABreach(RecycleRequest request) {
        try {
            log.warn("SLA breach detected for request {}. Driver: {}, Assigned at: {}", 
                    request.getId(), 
                    request.getAssignedDriverId(), 
                    request.getUpdatedAt());

            // 1. Notify Facility Owner
            if (request.getRecyclingFacility() != null && request.getRecyclingFacility().getUser() != null) {
                User facilityOwner = request.getRecyclingFacility().getUser();
                emailService.sendSLABreachEmailToFacility(
                        facilityOwner.getEmail(),
                        request.getRecyclingFacility().getName(),
                        request.getId().toString(),
                        slaHours,
                        "Your assigned driver" // Fallback name
                );
            }

            // 2. Notify Driver
            if (request.getAssignedDriverId() != null) {
                userRepository.findById(request.getAssignedDriverId()).ifPresent(driver -> {
                    String address = request.getPickupAddress() != null ? 
                            request.getPickupAddress().getAddress() + ", " + request.getPickupAddress().getCity() : 
                            "your registered pickup location";
                    
                    emailService.sendSLABreachEmailToDriver(
                            driver.getEmail(),
                            driver.getFullName(),
                            request.getId().toString(),
                            slaHours,
                            address
                    );
                    
                    // Update the "fallback name" for facility email if we found the driver
                    if (request.getRecyclingFacility() != null && request.getRecyclingFacility().getUser() != null) {
                        emailService.sendSLABreachEmailToFacility(
                                request.getRecyclingFacility().getUser().getEmail(),
                                request.getRecyclingFacility().getName(),
                                request.getId().toString(),
                                slaHours,
                                driver.getFullName()
                        );
                    }
                });
            }

            log.info("SLA breach notifications sent for request {}.", request.getId());

        } catch (Exception e) {
            log.error("Error handling SLA breach for request {}", request.getId(), e);
        }
    }

    /**
     * Check if a specific request has SLA breach
     */
    public boolean hasSLABreach(RecycleRequest request) {
        if (request.getAssignedDriverId() == null) {
            return false;
        }
        
        if (request.getFulfillmentStatus() != FulfillmentStatus.PICKUP_ASSIGNED) {
            return false;
        }

        LocalDateTime assignedAt = request.getUpdatedAt();
        LocalDateTime slaDeadline = assignedAt.plusHours(slaHours);
        
        return LocalDateTime.now().isAfter(slaDeadline);
    }

    /**
     * Get hours remaining until SLA breach
     */
    public long getHoursUntilSLABreach(RecycleRequest request) {
        if (request.getAssignedDriverId() == null) {
            return -1;
        }

        LocalDateTime assignedAt = request.getUpdatedAt();
        LocalDateTime slaDeadline = assignedAt.plusHours(slaHours);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(slaDeadline)) {
            return 0; // Already breached
        }

        return java.time.Duration.between(now, slaDeadline).toHours();
    }
}
