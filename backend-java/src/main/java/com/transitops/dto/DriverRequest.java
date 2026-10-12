package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverRequest {
    private String name;

    @JsonProperty("license_number")
    private String licenseNumber;

    @JsonProperty("license_category")
    private String licenseCategory;

    @JsonProperty("license_expiry")
    private LocalDate licenseExpiry;

    private String contact;

    @JsonProperty("safety_score")
    private Integer safetyScore;

    private String status;
}
