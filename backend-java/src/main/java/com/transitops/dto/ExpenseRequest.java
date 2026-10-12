package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseRequest {
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("trip_id")
    private Long tripId;

    private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
}
