package com.transitops.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Driver entity — maps to the `drivers` table.
 */
@Entity
@Table(name = "drivers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "license_category", nullable = false, length = 10)
    private String licenseCategory;

    @Column(name = "license_expiry", nullable = false)
    private LocalDate licenseExpiry;

    @Column(length = 20)
    private String contact;

    @Column(name = "safety_score")
    @Builder.Default
    private Integer safetyScore = 100;

    @Column(length = 20)
    @Builder.Default
    private String status = "Available";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
