package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard-kpis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardKPIs() {
        Map<String, Object> kpis = reportService.getDashboardKPIs();
        return ResponseEntity.ok(ApiResponse.ok(kpis));
    }

    @GetMapping("/fuel-efficiency")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFuelEfficiency() {
        List<Map<String, Object>> list = reportService.getFuelEfficiency();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/operational-cost")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOperationalCost() {
        List<Map<String, Object>> list = reportService.getOperationalCost();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/vehicle-roi")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVehicleROI() {
        List<Map<String, Object>> list = reportService.getVehicleROI();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/trip-trends")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTripTrends() {
        List<Map<String, Object>> list = reportService.getTripTrends();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
