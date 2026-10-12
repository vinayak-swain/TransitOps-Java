package com.transitops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateStatusRequest {
    @JsonProperty("is_active")
    private Boolean isActive;
}
