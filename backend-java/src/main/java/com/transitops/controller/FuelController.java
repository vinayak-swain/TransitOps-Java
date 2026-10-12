package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.FuelLogRequest;
import com.transitops.service.FuelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fuel")
public class FuelController {

    private final FuelService fuelService;

    public FuelController(FuelService fuelService) {
        this.fuelService = fuelService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFuelLogs(
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(name = "trip_id", required = false) Long tripId) {
        List<Map<String, Object>> list = fuelService.getFuelLogs(vehicleId, tripId);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFuelLog(@RequestBody FuelLogRequest req) {
        Map<String, Object> log = fuelService.createFuelLog(req);
        ApiResponse<Map<String, Object>> resp = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Fuel log created.")
                .data(log)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFuelLog(@PathVariable Long id) {
        fuelService.deleteFuelLog(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Fuel log deleted.")
                .build());
    }
}
