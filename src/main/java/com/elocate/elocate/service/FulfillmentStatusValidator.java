package com.elocate.elocate.service;

import com.elocate.elocate.dto.CreateRecycleRequestDto;
import com.elocate.elocate.model.enums.FulfillmentStatus;
import com.elocate.elocate.model.enums.FulfillmentType;
import com.elocate.elocate.model.UserAddress;
import com.elocate.elocate.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for validating fulfillment requirements and status transitions
 */
@Service
@RequiredArgsConstructor
public class FulfillmentStatusValidator {
    
    private final UserAddressRepository userAddressRepository;
    
    /**
     * Validate that the status transition is allowed
     * 
     * @param current Current fulfillment status
     * @param next Next fulfillment status
     * @throws IllegalArgumentException if transition is invalid
     */
    public void validateStatusTransition(FulfillmentStatus current, FulfillmentStatus next) {
        // Rule 1: Cannot transition from PICKUP status to DROP status
        if (current.isPickupStatus() && next.isDropStatus()) {
            throw new IllegalArgumentException(
                    "Invalid status transition: Cannot change from PICKUP status (" + current + 
                    ") to DROP status (" + next + ")");
        }
        
        // Rule 2: Cannot transition from DROP status to PICKUP status
        if (current.isDropStatus() && next.isPickupStatus()) {
            throw new IllegalArgumentException(
                    "Invalid status transition: Cannot change from DROP status (" + current + 
                    ") to PICKUP status (" + next + ")");
        }
        
        // Additional validation can be added here for specific transition rules
        // e.g., PICKUP_REQUESTED can only go to PICKUP_ASSIGNED or PICKUP_FAILED
    }
    
    /**
     * Validate pickup requirements and return the address to use
     * 
     * @param dto The create request DTO
     * @param userId User ID
     * @return The UserAddress entity to use (either provided or default)
     * @throws IllegalArgumentException if validation fails
     */
    public UserAddress validateAndGetPickupAddress(CreateRecycleRequestDto dto, UUID userId) {
        // If pickup address is provided, fetch and return it
        if (dto.getPickupAddressId() != null) {
            return userAddressRepository.findById(dto.getPickupAddressId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pickup address not found: " + dto.getPickupAddressId()));
        }
        
        // Otherwise, fetch user's default address
        return userAddressRepository
                .findByUserIdAndIsDefault(userId, true)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No pickup address provided and user has no default address. " +
                        "Please provide a pickup address or set a default address."));
    }
    
    /**
     * Validate drop-off requirements
     * 
     * @param dto The create request DTO
     * @throws IllegalArgumentException if validation fails
     */
    public void validateDropOffRequirements(CreateRecycleRequestDto dto) {
        if (dto.getFacilityId() == null) {
            throw new IllegalArgumentException(
                    "Facility ID is required for DROP_OFF fulfillment type");
        }
    }
    
    /**
     * Get the initial fulfillment status based on fulfillment type
     * 
     * @param fulfillmentType The fulfillment type
     * @return Initial status
     */
    public FulfillmentStatus getInitialStatus(FulfillmentType fulfillmentType) {
        return switch (fulfillmentType) {
            case PICKUP -> FulfillmentStatus.PICKUP_REQUESTED;
            case DROP_OFF -> FulfillmentStatus.DROP_PENDING;
        };
    }
}
