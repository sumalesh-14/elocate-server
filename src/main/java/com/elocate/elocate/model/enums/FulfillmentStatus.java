package com.elocate.elocate.model.enums;

/**
 * Detailed lifecycle tracking for logistics (pickup or drop-off)
 * Separate from RecycleStatus which tracks business outcome
 */
public enum FulfillmentStatus {

    // ========== PICKUP FLOW ==========
    /**
     * Initial state when pickup is requested
     */
    PICKUP_REQUESTED,

    /**
     * Pickup agent or facility has been assigned
     */
    PICKUP_ASSIGNED,

    /**
     * Driver is on the way to pick up the device
     */
    PICKUP_IN_PROGRESS,

    /**
     * Device has been successfully picked up
     */
    PICKUP_COMPLETED,

    /**
     * Pickup attempt failed (user unavailable, wrong address, etc.)
     */
    PICKUP_FAILED,

    // ========== DROP-OFF FLOW ==========
    /**
     * Waiting for user to drop off device at facility
     */
    DROP_PENDING,

    /**
     * User has dropped device at the facility
     */
    DROPPED_AT_FACILITY,

    /**
     * Facility has verified the drop-off
     */
    DROP_VERIFIED,

    REJECTED;

    /**
     * Check if this is a pickup-related status
     */
    public boolean isPickupStatus() {
        return this.name().startsWith("PICKUP_");
    }

    /**
     * Check if this is a drop-off-related status
     */
    public boolean isDropStatus() {
        return this.name().startsWith("DROP");
    }

    /**
     * Get user-friendly display text for UI
     */
    public String getDisplayText() {
        return switch (this) {
            case PICKUP_REQUESTED -> "Pickup requested";
            case PICKUP_ASSIGNED -> "Pickup agent assigned";
            case PICKUP_IN_PROGRESS -> "Driver on the way";
            case PICKUP_COMPLETED -> "Device picked up";
            case PICKUP_FAILED -> "Pickup failed";
            case DROP_PENDING -> "Drop pending";
            case DROPPED_AT_FACILITY -> "Dropped at center";
            case DROP_VERIFIED -> "Drop verified";
            case REJECTED -> "Rejected";
            default -> this.name();
        };
    }
}
