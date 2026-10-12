package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripCompleteRequest {
    @JsonProperty("actual_distance_km")
    private BigDecimal actualDistanceKm;

    @JsonProperty("fuel_consumed_l")
    private BigDecimal fuelConsumedL;

    @JsonProperty("final_odometer")
    private BigDecimal finalOdometer;
}
