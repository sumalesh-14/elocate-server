package com.elocate.elocate.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacilityResponse {
    private String address;
    private double distance;
    private String name;
    private int capacity;
    private double lon;
    private double lat;
    private String contact;
    private String time;
    private boolean verified;
}