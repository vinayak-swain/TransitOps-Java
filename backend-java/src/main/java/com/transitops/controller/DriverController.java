package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.DriverRequest;
import com.transitops.entity.Driver;
import com.transitops.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableDrivers() {
        List<Map<String, Object>> list = driverService.getAvailableDrivers();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDrivers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<Map<String, Object>> list = driverService.getDrivers(status, search);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDriverById(@PathVariable Long id) {
        Map<String, Object> driver = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.ok(driver));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Driver>> createDriver(@RequestBody DriverRequest req) {
        Driver driver = driverService.createDriver(req);
        ApiResponse<Driver> resp = ApiResponse.<Driver>builder()
                .success(true)
                .message("Driver created successfully.")
                .data(driver)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Driver>> updateDriver(@PathVariable Long id, @RequestBody DriverRequest req) {
        Driver driver = driverService.updateDriver(id, req);
        ApiResponse<Driver> resp = ApiResponse.<Driver>builder()
                .success(true)
                .message("Driver updated.")
                .data(driver)
                .build();
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Driver deleted.")
                .build());
    }
}
