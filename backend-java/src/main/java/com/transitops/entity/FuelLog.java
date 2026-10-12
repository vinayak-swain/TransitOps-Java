package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * FuelLog entity — maps to the `fuel_logs` table.
 * Note: total_cost is a GENERATED column in PostgreSQL (liters * cost_per_liter).
 * In JPA we compute it via a @Formula or @PostLoad, and mark insertable/updatable=false.
 */
@Entity
@Table(name = "fuel_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal liters;

    @Column(name = "cost_per_liter", nullable = false, precision = 10, scale = 4)
    private BigDecimal costPerLiter;

    @Column(name = "total_cost", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate date = LocalDate.now();

    @Column(name = "odometer_reading", precision = 10, scale = 2)
    private BigDecimal odometerReading;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
