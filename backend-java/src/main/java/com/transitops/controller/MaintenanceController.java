package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.MaintenanceCloseRequest;
import com.transitops.dto.MaintenanceRequest;
import com.transitops.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMaintenanceLogs(
            @RequestParam(required = false) String status,
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId) {
        List<Map<String, Object>> list = maintenanceService.getMaintenanceLogs(status, vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMaintenanceLog(@RequestBody MaintenanceRequest req) {
        Map<String, Object> log = maintenanceService.createMaintenanceLog(req);
        ApiResponse<Map<String, Object>> resp = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Maintenance log created. Vehicle status set to In Shop.")
                .data(log)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMaintenanceLog(
            @PathVariable Long id, @RequestBody MaintenanceRequest req) {
        Map<String, Object> log = maintenanceService.updateMaintenanceLog(id, req);
        ApiResponse<Map<String, Object>> resp = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Maintenance log updated.")
                .data(log)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeMaintenanceLog(
            @PathVariable Long id, @RequestBody(required = false) MaintenanceCloseRequest req) {
        maintenanceService.closeMaintenanceLog(id, req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Maintenance completed. Vehicle restored to Available.")
                .build());
    }
}
