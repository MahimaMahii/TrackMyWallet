package com.trackmywallet.service;

import com.trackmywallet.dto.BudgetRequest;
import com.trackmywallet.dto.BudgetResponse;
import com.trackmywallet.model.Budget;
import com.trackmywallet.model.User;
import com.trackmywallet.repository.BudgetRepository;
import com.trackmywallet.repository.ExpenseRepository;
import com.trackmywallet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BudgetService - Manages monthly budgets, calculations, progress tracking, and alerts.
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         ExpenseRepository expenseRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get budget for the current month and year.
     */
    @Transactional(readOnly = true)
    public BudgetResponse getCurrentMonthBudget(Integer userId) {
        LocalDate now = LocalDate.now();
        return getBudgetForMonth(userId, now.getYear(), now.getMonthValue())
                .orElse(null);
    }

    /**
     * Get budget for a specific month and year.
     */
    @Transactional(readOnly = true)
    public Optional<BudgetResponse> getBudgetForMonth(Integer userId, Integer year, Integer month) {
        return budgetRepository.findByUserUserIdAndBudgetYearAndBudgetMonth(userId, year, month)
                .map(b -> toResponse(b, userId));
    }

    /**
     * Get all budgets configured by the user.
     */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(Integer userId) {
        List<Budget> budgets = budgetRepository.findByUserUserIdOrderByBudgetYearDescBudgetMonthDesc(userId);
        return budgets.stream()
                .map(b -> toResponse(b, userId))
                .collect(Collectors.toList());
    }

    /**
     * Set (or update if already exists) a monthly budget.
     */
    @Transactional
    public BudgetResponse setOrUpdateBudget(Integer userId, BudgetRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Budget amount must be strictly greater than zero.");
        }

        Optional<Budget> existingOpt = budgetRepository.findByUserUserIdAndBudgetYearAndBudgetMonth(
                userId, request.getYear(), request.getMonth());

        Budget budget;
        if (existingOpt.isPresent()) {
            budget = existingOpt.get();
            budget.setAmount(request.getAmount());
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
            budget = new Budget(user, request.getYear(), request.getMonth(), request.getAmount());
        }

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved, userId);
    }

    /**
     * Update an existing budget by budget ID.
     */
    @Transactional
    public BudgetResponse updateBudget(Integer userId, Integer budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findByBudgetIdAndUserUserId(budgetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied."));

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Budget amount must be strictly greater than zero.");
        }

        budget.setAmount(request.getAmount());
        Budget updated = budgetRepository.save(budget);
        return toResponse(updated, userId);
    }

    /**
     * Delete a budget.
     */
    @Transactional
    public void deleteBudget(Integer userId, Integer budgetId) {
        Budget budget = budgetRepository.findByBudgetIdAndUserUserId(budgetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied."));
        budgetRepository.delete(budget);
    }

    /**
     * Build BudgetResponse calculating spent amount, remaining amount, and alert states.
     */
    public BudgetResponse toResponse(Budget budget, Integer userId) {
        BigDecimal spent = expenseRepository.sumByUserAndMonth(userId, budget.getBudgetYear(), budget.getBudgetMonth());
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remaining = budgetAmount.subtract(spent);

        double percentage = 0.0;
        if (budgetAmount.signum() > 0) {
            percentage = spent.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        boolean isExceeded = remaining.signum() < 0;
        boolean isNearLimit = !isExceeded && percentage >= 80.0;

        String statusMessage;
        if (isExceeded) {
            statusMessage = "⚠️ Budget exceeded by " + remaining.abs().setScale(2, RoundingMode.HALF_UP);
        } else if (isNearLimit) {
            statusMessage = "🔔 Approaching budget limit (" + String.format("%.1f", percentage) + "% used)";
        } else {
            statusMessage = "✅ On track (" + String.format("%.1f", percentage) + "% used)";
        }

        return new BudgetResponse(
                budget.getBudgetId(),
                budget.getBudgetYear(),
                budget.getBudgetMonth(),
                budgetAmount.setScale(2, RoundingMode.HALF_UP),
                spent.setScale(2, RoundingMode.HALF_UP),
                remaining.setScale(2, RoundingMode.HALF_UP),
                Math.round(percentage * 10.0) / 10.0,
                isExceeded,
                isNearLimit,
                statusMessage
        );
    }
}
