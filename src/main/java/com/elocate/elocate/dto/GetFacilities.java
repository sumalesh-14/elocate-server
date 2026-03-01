package com.elocate.elocate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFacilities {

    private Double userLatitude;

    private Double userLongitude;

    private Double distance; // Distance in kilometers

}
