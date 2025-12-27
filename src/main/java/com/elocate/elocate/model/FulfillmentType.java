package com.elocate.elocate.model;

/**
 * Defines how the device will be handed over for recycling
 */
public enum FulfillmentType {
    /**
     * Device will be picked up from user's address
     */
    PICKUP,
    
    /**
     * User will drop off device at a recycling facility
     */
    DROP_OFF
}
