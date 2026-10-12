package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceCloseRequest {
    @JsonProperty("end_date")
    private LocalDate endDate;
}
