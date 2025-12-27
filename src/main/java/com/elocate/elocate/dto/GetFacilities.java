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

    @NotNull(message = "userLatitute is required")
    private Double userLatitute;

    @NotNull(message = "userLongitude is required")
    private Double userLongitude;

}
