package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.VehicleRequest;
import com.transitops.entity.Vehicle;
import com.transitops.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableVehicles() {
        List<Map<String, Object>> list = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVehicles(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search) {
        List<Map<String, Object>> list = vehicleService.getVehicles(status, type, region, search);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVehicleById(@PathVariable Long id) {
        Map<String, Object> vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.ok(vehicle));
    }

    @PostMapping
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@RequestBody VehicleRequest req) {
        Vehicle vehicle = vehicleService.createVehicle(req);
        ApiResponse<Vehicle> resp = ApiResponse.<Vehicle>builder()
                .success(true)
                .message("Vehicle created successfully.")
                .data(vehicle)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(@PathVariable Long id, @RequestBody VehicleRequest req) {
        Vehicle vehicle = vehicleService.updateVehicle(id, req);
        ApiResponse<Vehicle> resp = ApiResponse.<Vehicle>builder()
                .success(true)
                .message("Vehicle updated.")
                .data(vehicle)
                .build();
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Vehicle deleted.")
                .build());
    }
}
