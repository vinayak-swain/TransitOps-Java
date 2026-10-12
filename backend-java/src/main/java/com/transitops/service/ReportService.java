package com.transitops.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getDashboardKPIs() {
        String vehiclesSql = """
            SELECT
              COUNT(*) AS total_vehicles,
              COUNT(*) FILTER (WHERE status = 'Available') AS available_vehicles,
              COUNT(*) FILTER (WHERE status = 'On Trip') AS on_trip_vehicles,
              COUNT(*) FILTER (WHERE status = 'In Shop') AS in_shop_vehicles,
              COUNT(*) FILTER (WHERE status = 'Retired') AS retired_vehicles
            FROM vehicles
        """;

        String tripsSql = """
            SELECT
              COUNT(*) AS total_trips,
              COUNT(*) FILTER (WHERE status = 'Dispatched') AS active_trips,
              COUNT(*) FILTER (WHERE status = 'Draft') AS pending_trips,
              COUNT(*) FILTER (WHERE status = 'Completed') AS completed_trips,
              COUNT(*) FILTER (WHERE status = 'Cancelled') AS cancelled_trips,
              COALESCE(SUM(revenue) FILTER (WHERE status = 'Completed'), 0) AS total_revenue
            FROM trips
        """;

        String driversSql = """
            SELECT
              COUNT(*) AS total_drivers,
              COUNT(*) FILTER (WHERE status = 'Available') AS available_drivers,
              COUNT(*) FILTER (WHERE status = 'On Trip') AS on_duty_drivers,
              COUNT(*) FILTER (WHERE license_expiry <= CURRENT_DATE + INTERVAL '30 days' AND license_expiry > CURRENT_DATE) AS expiring_soon
            FROM drivers
        """;

        String utilizationSql = """
            SELECT
              CASE WHEN COUNT(*) > 0
              THEN ROUND((COUNT(*) FILTER (WHERE status = 'On Trip')::numeric / COUNT(*)::numeric * 100), 1)
              ELSE 0 END AS fleet_utilization_pct
            FROM vehicles WHERE status != 'Retired'
        """;

        Map<String, Object> vehicles = jdbcTemplate.queryForMap(vehiclesSql);
        Map<String, Object> trips = jdbcTemplate.queryForMap(tripsSql);
        Map<String, Object> drivers = jdbcTemplate.queryForMap(driversSql);
        Map<String, Object> utilRow = jdbcTemplate.queryForMap(utilizationSql);

        Object utilVal = utilRow.get("fleet_utilization_pct");
        double fleetUtilizationPct = 0.0;
        if (utilVal instanceof Number n) {
            fleetUtilizationPct = n.doubleValue();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("vehicles", vehicles);
        result.put("trips", trips);
        result.put("drivers", drivers);
        result.put("fleet_utilization_pct", fleetUtilizationPct);

        return result;
    }

    public List<Map<String, Object>> getFuelEfficiency() {
        String sql = """
            SELECT
              v.id, v.registration_number, v.name, v.type,
              COALESCE(SUM(fl.liters), 0) AS total_liters,
              COALESCE(SUM(fl.total_cost), 0) AS total_fuel_cost,
              COALESCE(SUM(t.actual_distance_km) FILTER (WHERE t.status = 'Completed'), 0) AS total_distance,
              CASE
                WHEN COALESCE(SUM(fl.liters), 0) > 0
                THEN ROUND(COALESCE(SUM(t.actual_distance_km) FILTER (WHERE t.status = 'Completed'), 0) / SUM(fl.liters), 2)
                ELSE 0
              END AS km_per_liter
            FROM vehicles v
            LEFT JOIN fuel_logs fl ON fl.vehicle_id = v.id
            LEFT JOIN trips t ON t.vehicle_id = v.id
            GROUP BY v.id, v.registration_number, v.name, v.type
            ORDER BY km_per_liter DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getOperationalCost() {
        String sql = """
            SELECT
              v.id, v.registration_number, v.name, v.type,
              COALESCE(SUM(fl.total_cost), 0) AS fuel_cost,
              COALESCE(SUM(ml.cost) FILTER (WHERE ml.status = 'Closed'), 0) AS maintenance_cost,
              COALESCE(SUM(e.amount) FILTER (WHERE e.category != 'Fuel'), 0) AS other_expenses,
              COALESCE(SUM(fl.total_cost), 0) + COALESCE(SUM(ml.cost) FILTER (WHERE ml.status = 'Closed'), 0) + COALESCE(SUM(e.amount) FILTER (WHERE e.category != 'Fuel'), 0) AS total_operational_cost
            FROM vehicles v
            LEFT JOIN fuel_logs fl ON fl.vehicle_id = v.id
            LEFT JOIN maintenance_logs ml ON ml.vehicle_id = v.id
            LEFT JOIN expenses e ON e.vehicle_id = v.id
            GROUP BY v.id, v.registration_number, v.name, v.type
            ORDER BY total_operational_cost DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getVehicleROI() {
        String sql = """
            SELECT
              v.id, v.registration_number, v.name, v.type, v.acquisition_cost,
              COALESCE(SUM(t.revenue) FILTER (WHERE t.status = 'Completed'), 0) AS total_revenue,
              COALESCE(SUM(fl.total_cost), 0) AS fuel_cost,
              COALESCE(SUM(ml.cost) FILTER (WHERE ml.status = 'Closed'), 0) AS maintenance_cost,
              CASE
                WHEN v.acquisition_cost > 0 THEN
                  ROUND(
                    (COALESCE(SUM(t.revenue) FILTER (WHERE t.status = 'Completed'), 0)
                     - COALESCE(SUM(fl.total_cost), 0)
                     - COALESCE(SUM(ml.cost) FILTER (WHERE ml.status = 'Closed'), 0))
                    / v.acquisition_cost * 100, 2
                  )
                ELSE 0
              END AS roi_pct
            FROM vehicles v
            LEFT JOIN trips t ON t.vehicle_id = v.id
            LEFT JOIN fuel_logs fl ON fl.vehicle_id = v.id
            LEFT JOIN maintenance_logs ml ON ml.vehicle_id = v.id
            GROUP BY v.id, v.registration_number, v.name, v.type, v.acquisition_cost
            ORDER BY roi_pct DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTripTrends() {
        String sql = """
            SELECT
              TO_CHAR(DATE_TRUNC('month', created_at), 'Mon YYYY') AS month,
              DATE_TRUNC('month', created_at) AS month_date,
              COUNT(*) AS total_trips,
              COUNT(*) FILTER (WHERE status = 'Completed') AS completed,
              COUNT(*) FILTER (WHERE status = 'Cancelled') AS cancelled,
              COALESCE(SUM(revenue) FILTER (WHERE status = 'Completed'), 0) AS revenue
            FROM trips
            WHERE created_at >= NOW() - INTERVAL '6 months'
            GROUP BY DATE_TRUNC('month', created_at)
            ORDER BY month_date ASC
        """;
        return jdbcTemplate.queryForList(sql);
    }
}
