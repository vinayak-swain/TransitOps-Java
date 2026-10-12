package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FuelLogRequest {
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("trip_id")
    private Long tripId;

    private BigDecimal liters;

    @JsonProperty("cost_per_liter")
    private BigDecimal costPerLiter;

    private LocalDate date;

    @JsonProperty("odometer_reading")
    private BigDecimal odometerReading;
}
