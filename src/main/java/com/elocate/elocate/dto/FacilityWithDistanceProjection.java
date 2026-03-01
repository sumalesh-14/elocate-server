package com.elocate.elocate.dto;

public interface FacilityWithDistanceProjection {
    String getName();

    String getAddress();

    Integer getCapacity();

    Double getLatitude();

    Double getLongitude();

    String getContactNumber();

    String getOperatingHours();

    Boolean getIsVerified();

    Double getDistance();

    java.util.UUID getId();
}
