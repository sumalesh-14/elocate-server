package com.elocate.elocate.dto;

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

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 6;

    private Boolean verified;

}
