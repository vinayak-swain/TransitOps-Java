package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Vehicle entity — maps to the `vehicles` table.
 */
@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String model;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "max_load_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxLoadKg;

    @Column(name = "odometer_km", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal odometerKm = BigDecimal.ZERO;

    @Column(name = "acquisition_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(length = 20)
    @Builder.Default
    private String status = "Available";

    @Column(length = 100)
    private String region;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
