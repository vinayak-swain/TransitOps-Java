package com.transitops.service;

import com.transitops.dto.MaintenanceCloseRequest;
import com.transitops.dto.MaintenanceRequest;
import com.transitops.entity.MaintenanceLog;
import com.transitops.entity.Vehicle;
import com.transitops.repository.MaintenanceLogRepository;
import com.transitops.repository.VehicleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class MaintenanceService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleRepository vehicleRepository;
    private final JdbcTemplate jdbcTemplate;

    public MaintenanceService(MaintenanceLogRepository maintenanceLogRepository,
                              VehicleRepository vehicleRepository,
                              JdbcTemplate jdbcTemplate) {
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.vehicleRepository = vehicleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getMaintenanceLogs(String status, Long vehicleId) {
        StringBuilder sql = new StringBuilder("""
            SELECT ml.*, v.registration_number, v.name AS vehicle_name, v.type AS vehicle_type
            FROM maintenance_logs ml
            JOIN vehicles v ON v.id = ml.vehicle_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND ml.status = ?");
            params.add(status);
        }
        if (vehicleId != null) {
            sql.append(" AND ml.vehicle_id = ?");
            params.add(vehicleId);
        }

        sql.append(" ORDER BY ml.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> createMaintenanceLog(MaintenanceRequest req) {
        if (req.getVehicleId() == null || req.getType() == null || req.getStartDate() == null) {
            throw new IllegalArgumentException("Required fields: vehicle_id, type, start_date.");
        }

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found."));

        if ("On Trip".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalArgumentException("Vehicle is currently On Trip. Cannot add maintenance record.");
        }

        String sql = """
            INSERT INTO maintenance_logs (vehicle_id, type, description, cost, start_date)
            VALUES (?, ?, ?, ?, ?) RETURNING *
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                req.getVehicleId(),
                req.getType(),
                req.getDescription(),
                req.getCost() != null ? req.getCost() : BigDecimal.ZERO,
                req.getStartDate()
        );

        // Automatically set vehicle status to In Shop
        vehicle.setStatus("In Shop");
        vehicleRepository.save(vehicle);

        return rows.get(0);
    }

    @Transactional
    public Map<String, Object> updateMaintenanceLog(Long id, MaintenanceRequest req) {
        MaintenanceLog ml = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Maintenance log not found."));

        if (req.getType() != null) ml.setType(req.getType());
        if (req.getDescription() != null) ml.setDescription(req.getDescription());
        if (req.getCost() != null) ml.setCost(req.getCost());
        if (req.getStartDate() != null) ml.setStartDate(req.getStartDate());

        maintenanceLogRepository.save(ml);

        String sql = "SELECT * FROM maintenance_logs WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    @Transactional
    public void closeMaintenanceLog(Long id, MaintenanceCloseRequest req) {
        MaintenanceLog ml = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Maintenance log not found."));

        if ("Closed".equalsIgnoreCase(ml.getStatus())) {
            throw new IllegalArgumentException("Maintenance log is already closed.");
        }

        LocalDate endDate = (req != null && req.getEndDate() != null) ? req.getEndDate() : LocalDate.now();
        ml.setStatus("Closed");
        ml.setEndDate(endDate);
        maintenanceLogRepository.save(ml);

        Vehicle vehicle = ml.getVehicle();
        if (!"Retired".equalsIgnoreCase(vehicle.getStatus())) {
            vehicle.setStatus("Available");
            vehicleRepository.save(vehicle);
        }
    }
}
