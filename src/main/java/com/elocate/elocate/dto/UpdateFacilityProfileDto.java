package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFacilityProfileDto {

    private String facilityName;
    private String ownerName;
    private String operatingHours;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Integer capacity;
}
