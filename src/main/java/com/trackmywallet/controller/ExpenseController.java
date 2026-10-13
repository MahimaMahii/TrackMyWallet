package com.trackmywallet.controller;

import com.trackmywallet.dto.ApiResponse;
import com.trackmywallet.dto.ExpenseRequest;
import com.trackmywallet.dto.ExpenseResponse;
import com.trackmywallet.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ExpenseController - REST API endpoints for user expense management.
 * All operations require an authenticated session and only access the session user's expenses.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthController.SESSION_USER_ID) == null) {
            throw new IllegalStateException("User session not found. Please log in.");
        }
        return (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        Integer userId = getSessionUserId(request);
        List<ExpenseResponse> list = expenseService.getExpenses(userId, categoryId, startDate, endDate, keyword);
        return ResponseEntity.ok(ApiResponse.ok("Expenses retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            ExpenseResponse response = expenseService.getExpenseById(userId, id);
            return ResponseEntity.ok(ApiResponse.ok("Expense retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest expenseRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            ExpenseResponse created = expenseService.createExpense(userId, expenseRequest);
            return ResponseEntity.ok(ApiResponse.ok("Expense added successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to record expense: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ExpenseRequest expenseRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            ExpenseResponse updated = expenseService.updateExpense(userId, id, expenseRequest);
            return ResponseEntity.ok(ApiResponse.ok("Expense updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to update expense: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            expenseService.deleteExpense(userId, id);
            return ResponseEntity.ok(ApiResponse.ok("Expense deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete expense: " + e.getMessage()));
        }
    }
}
