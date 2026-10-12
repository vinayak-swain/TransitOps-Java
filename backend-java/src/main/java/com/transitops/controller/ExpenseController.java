package com.transitops.controller;

import com.transitops.dto.ApiResponse;
import com.transitops.dto.ExpenseRequest;
import com.transitops.service.FuelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final FuelService fuelService;

    public ExpenseController(FuelService fuelService) {
        this.fuelService = fuelService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExpenses(
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(required = false) String category) {
        List<Map<String, Object>> list = fuelService.getExpenses(vehicleId, category);
        return ResponseEntity.ok(ApiResponse.ok(list, list.size()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExpense(@RequestBody ExpenseRequest req) {
        Map<String, Object> expense = fuelService.createExpense(req);
        ApiResponse<Map<String, Object>> resp = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Expense recorded.")
                .data(expense)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
