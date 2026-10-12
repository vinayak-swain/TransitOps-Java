package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceRequest {
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    private String type;
    private String description;
    private BigDecimal cost;

    @JsonProperty("start_date")
    private LocalDate startDate;
}
