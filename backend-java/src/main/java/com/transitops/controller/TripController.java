package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.TripCompleteRequest;
import com.transitops.dto.TripRequest;
import com.transitops.security.SecurityUtils;
import com.transitops.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrips(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<Map<String, Object>> list = tripService.getTrips(status, search);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTripById(@PathVariable Long id) {
        Map<String, Object> trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.ok(trip));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTrip(@RequestBody TripRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> trip = tripService.createTrip(req, userId);
        ApiResponse<Map<String, Object>> resp = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Trip created successfully.")
                .data(trip)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse<Void>> dispatchTrip(@PathVariable Long id) {
        tripService.dispatchTrip(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Trip dispatched successfully. Vehicle and Driver are now On Trip.")
                .build());
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTrip(@PathVariable Long id, @RequestBody(required = false) TripCompleteRequest req) {
        tripService.completeTrip(id, req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Trip completed. Vehicle and Driver are now Available.")
                .build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTrip(@PathVariable Long id) {
        tripService.cancelTrip(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Trip cancelled. Vehicle and Driver restored to Available.")
                .build());
    }
}
