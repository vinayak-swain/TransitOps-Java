package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleRequest {
    @JsonProperty("registration_number")
    private String registrationNumber;

    private String name;
    private String model;
    private String type;

    @JsonProperty("max_load_kg")
    private BigDecimal maxLoadKg;

    @JsonProperty("odometer_km")
    private BigDecimal odometerKm;

    @JsonProperty("acquisition_cost")
    private BigDecimal acquisitionCost;

    private String status;
    private String region;
}
