package com.transitops.service;

import com.transitops.dto.VehicleRequest;
import com.transitops.entity.Vehicle;
import com.transitops.repository.VehicleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> VALID_TYPES = Set.of("Truck", "Van", "Car", "Motorcycle", "Bus", "Trailer");

    public VehicleService(VehicleRepository vehicleRepository, JdbcTemplate jdbcTemplate) {
        this.vehicleRepository = vehicleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getVehicles(String status, String type, String region, String search) {
        StringBuilder sql = new StringBuilder("""
            SELECT v.*,
                COALESCE(SUM(fl.total_cost), 0) AS total_fuel_cost,
                COALESCE(SUM(ml.cost) FILTER (WHERE ml.status = 'Closed'), 0) AS total_maintenance_cost,
                COALESCE(SUM(fl.liters), 0) AS total_fuel_liters,
                COUNT(DISTINCT t.id) FILTER (WHERE t.status = 'Completed') AS completed_trips
            FROM vehicles v
            LEFT JOIN fuel_logs fl ON fl.vehicle_id = v.id
            LEFT JOIN maintenance_logs ml ON ml.vehicle_id = v.id
            LEFT JOIN trips t ON t.vehicle_id = v.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND v.status = ?");
            params.add(status);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND v.type = ?");
            params.add(type);
        }
        if (region != null && !region.isBlank()) {
            sql.append(" AND v.region = ?");
            params.add(region);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (v.registration_number ILIKE ? OR v.name ILIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        sql.append(" GROUP BY v.id ORDER BY v.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getAvailableVehicles() {
        String sql = """
            SELECT id, registration_number, name, model, type, max_load_kg, odometer_km
            FROM vehicles
            WHERE status = 'Available'
            ORDER BY name
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getVehicleById(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM vehicles WHERE id = ?", id);
        if (rows.isEmpty()) {
            throw new NoSuchElementException("Vehicle not found.");
        }
        return rows.get(0);
    }

    @Transactional
    public Vehicle createVehicle(VehicleRequest req) {
        if (req.getRegistrationNumber() == null || req.getName() == null || req.getType() == null
                || req.getMaxLoadKg() == null || req.getAcquisitionCost() == null) {
            throw new IllegalArgumentException("Required fields: registration_number, name, type, max_load_kg, acquisition_cost.");
        }

        if (!VALID_TYPES.contains(req.getType())) {
            throw new IllegalArgumentException("Invalid vehicle type. Must be one of: " + String.join(", ", VALID_TYPES));
        }

        String regUpper = req.getRegistrationNumber().toUpperCase().trim();
        if (vehicleRepository.existsByRegistrationNumber(regUpper)) {
            throw new IllegalStateException("Registration number already exists.");
        }

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(regUpper)
                .name(req.getName().trim())
                .model(req.getModel())
                .type(req.getType())
                .maxLoadKg(req.getMaxLoadKg())
                .odometerKm(req.getOdometerKm() != null ? req.getOdometerKm() : BigDecimal.ZERO)
                .acquisitionCost(req.getAcquisitionCost())
                .status(req.getStatus() != null ? req.getStatus() : "Available")
                .region(req.getRegion())
                .build();

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateVehicle(Long id, VehicleRequest req) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found."));

        if (req.getName() != null) vehicle.setName(req.getName().trim());
        if (req.getModel() != null) vehicle.setModel(req.getModel());
        if (req.getType() != null) vehicle.setType(req.getType());
        if (req.getMaxLoadKg() != null) vehicle.setMaxLoadKg(req.getMaxLoadKg());
        if (req.getOdometerKm() != null) vehicle.setOdometerKm(req.getOdometerKm());
        if (req.getAcquisitionCost() != null) vehicle.setAcquisitionCost(req.getAcquisitionCost());
        if (req.getStatus() != null) vehicle.setStatus(req.getStatus());
        if (req.getRegion() != null) vehicle.setRegion(req.getRegion());

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found."));

        if ("On Trip".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalArgumentException("Cannot delete a vehicle that is On Trip.");
        }

        vehicleRepository.delete(vehicle);
    }
}
