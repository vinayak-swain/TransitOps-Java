package com.transitops.service;

import com.transitops.dto.TripCompleteRequest;
import com.transitops.dto.TripRequest;
import com.transitops.entity.Driver;
import com.transitops.entity.Trip;
import com.transitops.entity.Vehicle;
import com.transitops.repository.DriverRepository;
import com.transitops.repository.TripRepository;
import com.transitops.repository.VehicleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final JdbcTemplate jdbcTemplate;

    public TripService(TripRepository tripRepository, VehicleRepository vehicleRepository,
                       DriverRepository driverRepository, JdbcTemplate jdbcTemplate) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getTrips(String status, String search) {
        StringBuilder sql = new StringBuilder("""
            SELECT t.*,
                v.registration_number, v.name AS vehicle_name, v.type AS vehicle_type,
                d.name AS driver_name, d.license_number,
                u.name AS created_by_name
            FROM trips t
            LEFT JOIN vehicles v ON v.id = t.vehicle_id
            LEFT JOIN drivers d ON d.id = t.driver_id
            LEFT JOIN users u ON u.id = t.created_by
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (t.source ILIKE ? OR t.destination ILIKE ? OR v.registration_number ILIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        sql.append(" ORDER BY t.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public Map<String, Object> getTripById(Long id) {
        String sql = """
            SELECT t.*, v.registration_number, v.name AS vehicle_name, v.type AS vehicle_type, v.max_load_kg,
                   d.name AS driver_name, d.license_number, d.license_expiry
            FROM trips t
            LEFT JOIN vehicles v ON v.id = t.vehicle_id
            LEFT JOIN drivers d ON d.id = t.driver_id
            WHERE t.id = ?
        """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) {
            throw new NoSuchElementException("Trip not found.");
        }
        return rows.get(0);
    }

    @Transactional
    public Map<String, Object> createTrip(TripRequest req, Long userId) {
        if (req.getSource() == null || req.getDestination() == null || req.getVehicleId() == null
                || req.getDriverId() == null || req.getCargoWeightKg() == null || req.getPlannedDistanceKm() == null) {
            throw new IllegalArgumentException("Required fields: source, destination, vehicle_id, driver_id, cargo_weight_kg, planned_distance_km.");
        }

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found."));

        if (!"Available".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalArgumentException("Vehicle is not available. Current status: " + vehicle.getStatus());
        }
        if (req.getCargoWeightKg().compareTo(vehicle.getMaxLoadKg()) > 0) {
            throw new IllegalArgumentException(String.format("Cargo weight (%s kg) exceeds vehicle max load (%s kg).",
                    req.getCargoWeightKg(), vehicle.getMaxLoadKg()));
        }

        Driver driver = driverRepository.findById(req.getDriverId())
                .orElseThrow(() -> new NoSuchElementException("Driver not found."));

        if (!"Available".equalsIgnoreCase(driver.getStatus())) {
            throw new IllegalArgumentException("Driver is not available. Current status: " + driver.getStatus());
        }
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Driver license is expired. Cannot assign to trip.");
        }

        String sql = """
            INSERT INTO trips (source, destination, vehicle_id, driver_id, cargo_weight_kg, planned_distance_km, revenue, notes, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                req.getSource(),
                req.getDestination(),
                req.getVehicleId(),
                req.getDriverId(),
                req.getCargoWeightKg(),
                req.getPlannedDistanceKm(),
                req.getRevenue() != null ? req.getRevenue() : BigDecimal.ZERO,
                req.getNotes(),
                userId
        );

        return rows.get(0);
    }

    @Transactional
    public void dispatchTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found."));

        if (!"Draft".equalsIgnoreCase(trip.getStatus())) {
            throw new IllegalArgumentException("Trip cannot be dispatched. Current status: " + trip.getStatus());
        }

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicle().getId())
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found."));
        Driver driver = driverRepository.findById(trip.getDriver().getId())
                .orElseThrow(() -> new NoSuchElementException("Driver not found."));

        if (!"Available".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalArgumentException("Vehicle is no longer available. Status: " + vehicle.getStatus());
        }
        if (!"Available".equalsIgnoreCase(driver.getStatus())) {
            throw new IllegalArgumentException("Driver is no longer available. Status: " + driver.getStatus());
        }
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Driver license has expired.");
        }

        trip.setStatus("Dispatched");
        trip.setDispatchedAt(OffsetDateTime.now());
        tripRepository.save(trip);

        vehicle.setStatus("On Trip");
        vehicleRepository.save(vehicle);

        driver.setStatus("On Trip");
        driverRepository.save(driver);
    }

    @Transactional
    public void completeTrip(Long id, TripCompleteRequest req) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found."));

        if (!"Dispatched".equalsIgnoreCase(trip.getStatus())) {
            throw new IllegalArgumentException("Trip cannot be completed. Current status: " + trip.getStatus());
        }

        BigDecimal actualDistance = (req != null && req.getActualDistanceKm() != null)
                ? req.getActualDistanceKm()
                : trip.getPlannedDistanceKm();

        trip.setStatus("Completed");
        trip.setActualDistanceKm(actualDistance);
        if (req != null && req.getFuelConsumedL() != null) {
            trip.setFuelConsumedL(req.getFuelConsumedL());
        }
        trip.setCompletedAt(OffsetDateTime.now());
        tripRepository.save(trip);

        Vehicle vehicle = trip.getVehicle();
        vehicle.setStatus("Available");
        vehicle.setOdometerKm(vehicle.getOdometerKm().add(actualDistance));
        vehicleRepository.save(vehicle);

        Driver driver = trip.getDriver();
        driver.setStatus("Available");
        driverRepository.save(driver);
    }

    @Transactional
    public void cancelTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found."));

        if (!List.of("Draft", "Dispatched").contains(trip.getStatus())) {
            throw new IllegalArgumentException("Trip cannot be cancelled. Current status: " + trip.getStatus());
        }

        boolean wasDispatched = "Dispatched".equalsIgnoreCase(trip.getStatus());

        trip.setStatus("Cancelled");
        tripRepository.save(trip);

        if (wasDispatched) {
            Vehicle vehicle = trip.getVehicle();
            vehicle.setStatus("Available");
            vehicleRepository.save(vehicle);

            Driver driver = trip.getDriver();
            driver.setStatus("Available");
            driverRepository.save(driver);
        }
    }
}
