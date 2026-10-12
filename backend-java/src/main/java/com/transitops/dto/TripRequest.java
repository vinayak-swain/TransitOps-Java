package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequest {
    private String source;
    private String destination;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("driver_id")
    private Long driverId;

    @JsonProperty("cargo_weight_kg")
    private BigDecimal cargoWeightKg;

    @JsonProperty("planned_distance_km")
    private BigDecimal plannedDistanceKm;

    private BigDecimal revenue;
    private String notes;
}
