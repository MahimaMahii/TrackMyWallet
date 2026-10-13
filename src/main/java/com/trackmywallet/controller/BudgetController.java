package com.trackmywallet.controller;

import com.trackmywallet.dto.ApiResponse;
import com.trackmywallet.dto.BudgetRequest;
import com.trackmywallet.dto.BudgetResponse;
import com.trackmywallet.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BudgetController - REST API for monthly budget management.
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthController.SESSION_USER_ID) == null) {
            throw new IllegalStateException("User session not found. Please log in.");
        }
        return (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<BudgetResponse>> getCurrentBudget(HttpServletRequest request) {
        Integer userId = getSessionUserId(request);
        BudgetResponse budget = budgetService.getCurrentMonthBudget(userId);
        if (budget == null) {
            return ResponseEntity.ok(ApiResponse.ok("No budget set for current month", null));
        }
        return ResponseEntity.ok(ApiResponse.ok("Current budget retrieved", budget));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        Integer userId = getSessionUserId(request);

        if (year != null && month != null) {
            BudgetResponse specific = budgetService.getBudgetForMonth(userId, year, month).orElse(null);
            List<BudgetResponse> result = specific != null ? List.of(specific) : List.of();
            return ResponseEntity.ok(ApiResponse.ok("Budget retrieved", result));
        }

        List<BudgetResponse> list = budgetService.getAllBudgets(userId);
        return ResponseEntity.ok(ApiResponse.ok("Budgets retrieved successfully", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> setBudget(
            @Valid @RequestBody BudgetRequest budgetRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            BudgetResponse response = budgetService.setOrUpdateBudget(userId, budgetRequest);
            return ResponseEntity.ok(ApiResponse.ok("Budget saved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to save budget: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable("id") Integer id,
            @Valid @RequestBody BudgetRequest budgetRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            BudgetResponse response = budgetService.updateBudget(userId, id, budgetRequest);
            return ResponseEntity.ok(ApiResponse.ok("Budget updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to update budget: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            budgetService.deleteBudget(userId, id);
            return ResponseEntity.ok(ApiResponse.ok("Budget removed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete budget: " + e.getMessage()));
        }
    }
}
