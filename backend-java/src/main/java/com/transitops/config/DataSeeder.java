package com.transitops.config;

import com.transitops.entity.Driver;
import com.transitops.entity.User;
import com.transitops.entity.Vehicle;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.UserRepository;
import com.transitops.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DataSeeder — mirrors backend/src/config/seed.js.
 * Automatically seeds demo accounts, vehicles, drivers, and records
 * on first startup if users table is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(UserRepository userRepository,
                      VehicleRepository vehicleRepository,
                      DriverRepository driverRepository,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            Long userCount = userRepository.count();
            if (userCount > 0) {
                log.info("Database already seeded ({} users found). Skipping DataSeeder.", userCount);
                return;
            }

            log.info("🌱 Seeding initial demo data for TransitOps...");
            String defaultPassword = passwordEncoder.encode("password123");

            // Seed Users
            User fleetManager = User.builder()
                    .name("Fleet Manager")
                    .email("fleet@transitops.com")
                    .passwordHash(defaultPassword)
                    .role("fleet_manager")
                    .isActive(true)
                    .provider("local")
                    .build();

            User dispatcher = User.builder()
                    .name("John Dispatcher")
                    .email("driver@transitops.com")
                    .passwordHash(defaultPassword)
                    .role("dispatcher")
                    .isActive(true)
                    .provider("local")
                    .build();

            User safety = User.builder()
                    .name("Safety Officer")
                    .email("safety@transitops.com")
                    .passwordHash(defaultPassword)
                    .role("safety_officer")
                    .isActive(true)
                    .provider("local")
                    .build();

            User finance = User.builder()
                    .name("Finance Analyst")
                    .email("finance@transitops.com")
                    .passwordHash(defaultPassword)
                    .role("financial_analyst")
                    .isActive(true)
                    .provider("local")
                    .build();

            userRepository.saveAll(List.of(fleetManager, dispatcher, safety, finance));

            // Seed Vehicles
            Vehicle v1 = Vehicle.builder().registrationNumber("TRK-001").name("Titan Hauler").model("Ford F-650").type("Truck").maxLoadKg(BigDecimal.valueOf(5000)).odometerKm(BigDecimal.valueOf(12500)).acquisitionCost(BigDecimal.valueOf(75000)).status("Available").region("North").build();
            Vehicle v2 = Vehicle.builder().registrationNumber("VAN-002").name("Swift Cargo").model("Mercedes Sprinter").type("Van").maxLoadKg(BigDecimal.valueOf(1200)).odometerKm(BigDecimal.valueOf(8900)).acquisitionCost(BigDecimal.valueOf(45000)).status("Available").region("South").build();
            Vehicle v3 = Vehicle.builder().registrationNumber("TRK-003").name("Iron Horse").model("Volvo FH16").type("Truck").maxLoadKg(BigDecimal.valueOf(8000)).odometerKm(BigDecimal.valueOf(34200)).acquisitionCost(BigDecimal.valueOf(120000)).status("In Shop").region("East").build();
            Vehicle v4 = Vehicle.builder().registrationNumber("VAN-004").name("City Runner").model("Ford Transit").type("Van").maxLoadKg(BigDecimal.valueOf(900)).odometerKm(BigDecimal.valueOf(21000)).acquisitionCost(BigDecimal.valueOf(38000)).status("Available").region("West").build();
            Vehicle v5 = Vehicle.builder().registrationNumber("TRK-005").name("Mountain Beast").model("Scania R450").type("Truck").maxLoadKg(BigDecimal.valueOf(10000)).odometerKm(BigDecimal.valueOf(55000)).acquisitionCost(BigDecimal.valueOf(150000)).status("Available").region("North").build();
            Vehicle v6 = Vehicle.builder().registrationNumber("CAR-006").name("Executive Sedan").model("Toyota Camry").type("Car").maxLoadKg(BigDecimal.valueOf(300)).odometerKm(BigDecimal.valueOf(15000)).acquisitionCost(BigDecimal.valueOf(25000)).status("Available").region("South").build();
            Vehicle v7 = Vehicle.builder().registrationNumber("BUS-007").name("Commuter Plus").model("Tata Starbus").type("Bus").maxLoadKg(BigDecimal.valueOf(2000)).odometerKm(BigDecimal.valueOf(40000)).acquisitionCost(BigDecimal.valueOf(85000)).status("Retired").region("East").build();

            vehicleRepository.saveAll(List.of(v1, v2, v3, v4, v5, v6, v7));

            // Seed Drivers
            LocalDate validExpiry = LocalDate.now().plusYears(2);
            LocalDate expiredExpiry = LocalDate.now().minusMonths(3);

            Driver d1 = Driver.builder().name("Alex Johnson").licenseNumber("DL-2024-001").licenseCategory("HMV").licenseExpiry(validExpiry).contact("+91-9876543210").safetyScore(95).status("Available").build();
            Driver d2 = Driver.builder().name("Maria Garcia").licenseNumber("DL-2024-002").licenseCategory("LMV").licenseExpiry(validExpiry).contact("+91-9876543211").safetyScore(88).status("Available").build();
            Driver d3 = Driver.builder().name("Bob Smith").licenseNumber("DL-2023-003").licenseCategory("HMV").licenseExpiry(expiredExpiry).contact("+91-9876543212").safetyScore(72).status("Off Duty").build();
            Driver d4 = Driver.builder().name("Priya Sharma").licenseNumber("DL-2024-004").licenseCategory("HMV").licenseExpiry(validExpiry).contact("+91-9876543213").safetyScore(91).status("Available").build();
            Driver d5 = Driver.builder().name("Carlos Ruiz").licenseNumber("DL-2024-005").licenseCategory("LMV").licenseExpiry(validExpiry).contact("+91-9876543214").safetyScore(85).status("Available").build();
            Driver d6 = Driver.builder().name("Tom Wilson").licenseNumber("DL-2022-006").licenseCategory("HMV").licenseExpiry(expiredExpiry).contact("+91-9876543215").safetyScore(60).status("Suspended").build();

            driverRepository.saveAll(List.of(d1, d2, d3, d4, d5, d6));

            // Seed demo trips, fuel logs, maintenance, and expenses
            jdbcTemplate.execute("""
                INSERT INTO trips (source, destination, vehicle_id, driver_id, cargo_weight_kg, planned_distance_km, actual_distance_km, fuel_consumed_l, revenue, status, dispatched_at, completed_at, created_by)
                VALUES ('Mumbai', 'Pune',
                  (SELECT id FROM vehicles WHERE registration_number = 'VAN-002'),
                  (SELECT id FROM drivers WHERE license_number = 'DL-2024-001'),
                  800, 150, 148, 18.5, 12000, 'Completed', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days',
                  (SELECT id FROM users WHERE email = 'fleet@transitops.com'));

                INSERT INTO fuel_logs (vehicle_id, trip_id, liters, cost_per_liter, date, odometer_reading) VALUES
                ((SELECT id FROM vehicles WHERE registration_number = 'VAN-002'), (SELECT id FROM trips WHERE source = 'Mumbai' LIMIT 1), 18.5, 95.50, CURRENT_DATE - 4, 8918),
                ((SELECT id FROM vehicles WHERE registration_number = 'VAN-002'), NULL, 40, 94.00, CURRENT_DATE - 10, 8870),
                ((SELECT id FROM vehicles WHERE registration_number = 'TRK-001'), NULL, 65, 93.00, CURRENT_DATE - 7, 12450);

                INSERT INTO maintenance_logs (vehicle_id, type, description, cost, start_date, status) VALUES
                ((SELECT id FROM vehicles WHERE registration_number = 'TRK-003'), 'Engine Overhaul', 'Complete engine overhaul and timing belt replacement', 25000, CURRENT_DATE - 2, 'Active'),
                ((SELECT id FROM vehicles WHERE registration_number = 'TRK-001'), 'Oil Change', 'Routine oil and filter change', 1500, CURRENT_DATE - 30, 'Closed');

                INSERT INTO expenses (vehicle_id, trip_id, category, amount, description, date) VALUES
                ((SELECT id FROM vehicles WHERE registration_number = 'VAN-002'), (SELECT id FROM trips WHERE source = 'Mumbai' LIMIT 1), 'Toll', 250, 'Mumbai-Pune expressway toll', CURRENT_DATE - 4),
                ((SELECT id FROM vehicles WHERE registration_number = 'TRK-001'), NULL, 'Insurance', 45000, 'Annual insurance premium', CURRENT_DATE - 15);
            """);

            log.info("✅ Database seeded successfully!");
            log.info("📧 Demo accounts: fleet@transitops.com, driver@transitops.com, safety@transitops.com, finance@transitops.com | Password: password123");

        } catch (Exception ex) {
            log.warn("Notice during DataSeeder execution: {}", ex.getMessage());
        }
    }
}
