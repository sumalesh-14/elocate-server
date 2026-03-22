package com.elocate.elocate.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DriverResponseDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String vehicleNumber;
    private String availability;
    private String vehicleType;
    private UUID facilityId;
}
