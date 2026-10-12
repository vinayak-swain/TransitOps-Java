package com.transitops.service;

import com.transitops.dto.ExpenseRequest;
import com.transitops.dto.FuelLogRequest;
import com.transitops.entity.FuelLog;
import com.transitops.repository.ExpenseRepository;
import com.transitops.repository.FuelLogRepository;
import com.transitops.repository.VehicleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class FuelService {

    private final FuelLogRepository fuelLogRepository;
    private final ExpenseRepository expenseRepository;
    private final VehicleRepository vehicleRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> VALID_CATEGORIES = Set.of("Toll", "Maintenance", "Fuel", "Insurance", "Other");

    public FuelService(FuelLogRepository fuelLogRepository,
                       ExpenseRepository expenseRepository,
                       VehicleRepository vehicleRepository,
                       JdbcTemplate jdbcTemplate) {
        this.fuelLogRepository = fuelLogRepository;
        this.expenseRepository = expenseRepository;
        this.vehicleRepository = vehicleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getFuelLogs(Long vehicleId, Long tripId) {
        StringBuilder sql = new StringBuilder("""
            SELECT fl.*, v.registration_number, v.name AS vehicle_name, t.source, t.destination
            FROM fuel_logs fl
            JOIN vehicles v ON v.id = fl.vehicle_id
            LEFT JOIN trips t ON t.id = fl.trip_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (vehicleId != null) {
            sql.append(" AND fl.vehicle_id = ?");
            params.add(vehicleId);
        }
        if (tripId != null) {
            sql.append(" AND fl.trip_id = ?");
            params.add(tripId);
        }

        sql.append(" ORDER BY fl.date DESC, fl.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> createFuelLog(FuelLogRequest req) {
        if (req.getVehicleId() == null || req.getLiters() == null || req.getCostPerLiter() == null) {
            throw new IllegalArgumentException("Required fields: vehicle_id, liters, cost_per_liter.");
        }
        if (req.getLiters().compareTo(BigDecimal.ZERO) <= 0 || req.getCostPerLiter().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Liters and cost_per_liter must be positive.");
        }

        if (!vehicleRepository.existsById(req.getVehicleId())) {
            throw new NoSuchElementException("Vehicle not found.");
        }

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        String sql = """
            INSERT INTO fuel_logs (vehicle_id, trip_id, liters, cost_per_liter, date, odometer_reading)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING *
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                req.getVehicleId(),
                req.getTripId(),
                req.getLiters(),
                req.getCostPerLiter(),
                date,
                req.getOdometerReading()
        );

        return rows.get(0);
    }

    @Transactional
    public void deleteFuelLog(Long id) {
        if (!fuelLogRepository.existsById(id)) {
            throw new NoSuchElementException("Fuel log not found.");
        }
        fuelLogRepository.deleteById(id);
    }

    public List<Map<String, Object>> getExpenses(Long vehicleId, String category) {
        StringBuilder sql = new StringBuilder("""
            SELECT e.*, v.registration_number, v.name AS vehicle_name, t.source, t.destination
            FROM expenses e
            JOIN vehicles v ON v.id = e.vehicle_id
            LEFT JOIN trips t ON t.id = e.trip_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (vehicleId != null) {
            sql.append(" AND e.vehicle_id = ?");
            params.add(vehicleId);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND e.category = ?");
            params.add(category);
        }

        sql.append(" ORDER BY e.date DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> createExpense(ExpenseRequest req) {
        if (req.getVehicleId() == null || req.getCategory() == null || req.getAmount() == null) {
            throw new IllegalArgumentException("Required fields: vehicle_id, category, amount.");
        }

        if (!VALID_CATEGORIES.contains(req.getCategory())) {
            throw new IllegalArgumentException("Invalid category. Must be one of: " + String.join(", ", VALID_CATEGORIES));
        }

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        String sql = """
            INSERT INTO expenses (vehicle_id, trip_id, category, amount, description, date)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING *
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                req.getVehicleId(),
                req.getTripId(),
                req.getCategory(),
                req.getAmount(),
                req.getDescription(),
                date
        );

        return rows.get(0);
    }
}
