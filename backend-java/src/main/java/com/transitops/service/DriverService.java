package com.transitops.service;

import com.transitops.dto.DriverRequest;
import com.transitops.entity.Driver;
import com.transitops.repository.DriverRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final JdbcTemplate jdbcTemplate;

    public DriverService(DriverRepository driverRepository, JdbcTemplate jdbcTemplate) {
        this.driverRepository = driverRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getDrivers(String status, String search) {
        StringBuilder sql = new StringBuilder("""
            SELECT d.*,
                COUNT(DISTINCT t.id) FILTER (WHERE t.status = 'Completed') AS completed_trips,
                CASE WHEN d.license_expiry < CURRENT_DATE THEN true ELSE false END AS license_expired,
                CASE WHEN d.license_expiry <= CURRENT_DATE + INTERVAL '30 days' AND d.license_expiry > CURRENT_DATE THEN true ELSE false END AS license_expiring_soon
            FROM drivers d
            LEFT JOIN trips t ON t.driver_id = d.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND d.status = ?");
            params.add(status);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (d.name ILIKE ? OR d.license_number ILIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        sql.append(" GROUP BY d.id ORDER BY d.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getAvailableDrivers() {
        String sql = """
            SELECT id, name, license_number, license_category, license_expiry, safety_score
            FROM drivers
            WHERE status = 'Available'
              AND license_expiry > CURRENT_DATE
            ORDER BY name
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getDriverById(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM drivers WHERE id = ?", id);
        if (rows.isEmpty()) {
            throw new NoSuchElementException("Driver not found.");
        }
        return rows.get(0);
    }

    @Transactional
    public Driver createDriver(DriverRequest req) {
        if (req.getName() == null || req.getLicenseNumber() == null || req.getLicenseCategory() == null || req.getLicenseExpiry() == null) {
            throw new IllegalArgumentException("Required fields: name, license_number, license_category, license_expiry.");
        }

        String licenseUpper = req.getLicenseNumber().toUpperCase().trim();
        if (driverRepository.existsByLicenseNumber(licenseUpper)) {
            throw new IllegalStateException("License number already registered.");
        }

        Driver driver = Driver.builder()
                .name(req.getName().trim())
                .licenseNumber(licenseUpper)
                .licenseCategory(req.getLicenseCategory())
                .licenseExpiry(req.getLicenseExpiry())
                .contact(req.getContact())
                .safetyScore(req.getSafetyScore() != null ? req.getSafetyScore() : 100)
                .status(req.getStatus() != null ? req.getStatus() : "Available")
                .build();

        return driverRepository.save(driver);
    }

    @Transactional
    public Driver updateDriver(Long id, DriverRequest req) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Driver not found."));

        if (req.getName() != null) driver.setName(req.getName().trim());
        if (req.getLicenseNumber() != null) driver.setLicenseNumber(req.getLicenseNumber().toUpperCase().trim());
        if (req.getLicenseCategory() != null) driver.setLicenseCategory(req.getLicenseCategory());
        if (req.getLicenseExpiry() != null) driver.setLicenseExpiry(req.getLicenseExpiry());
        if (req.getContact() != null) driver.setContact(req.getContact());
        if (req.getSafetyScore() != null) driver.setSafetyScore(req.getSafetyScore());
        if (req.getStatus() != null) driver.setStatus(req.getStatus());

        return driverRepository.save(driver);
    }

    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Driver not found."));

        if ("On Trip".equalsIgnoreCase(driver.getStatus())) {
            throw new IllegalArgumentException("Cannot delete a driver that is On Trip.");
        }

        driverRepository.delete(driver);
    }
}
