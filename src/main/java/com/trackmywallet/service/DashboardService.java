package com.trackmywallet.service;

import com.trackmywallet.dto.*;
import com.trackmywallet.model.Expense;
import com.trackmywallet.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * DashboardService - Assembles aggregate financial overview and analytics
 * strictly isolated to the authenticated user.
 */
@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    public DashboardService(ExpenseRepository expenseRepository,
                            ExpenseService expenseService,
                            BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    /**
     * Get complete dashboard summary for a specific month and year (or current month/year).
     */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Integer userId, Integer reqYear, Integer reqMonth) {
        LocalDate today = LocalDate.now();
        int year = (reqYear != null && reqYear >= 2000) ? reqYear : today.getYear();
        int month = (reqMonth != null && reqMonth >= 1 && reqMonth <= 12) ? reqMonth : today.getMonthValue();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setYear(year);
        response.setMonth(month);
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        response.setPeriodLabel(monthName + " " + year);

        // 1. Total expenses for this month
        BigDecimal totalThisMonth = expenseRepository.sumByUserAndMonth(userId, year, month);
        if (totalThisMonth == null) {
            totalThisMonth = BigDecimal.ZERO;
        }
        response.setTotalExpensesThisMonth(totalThisMonth.setScale(2, RoundingMode.HALF_UP));

        // 2. Lifetime total expenses
        BigDecimal lifetimeTotal = expenseRepository.sumTotalByUser(userId);
        if (lifetimeTotal == null) {
            lifetimeTotal = BigDecimal.ZERO;
        }
        response.setTotalExpensesLifetime(lifetimeTotal.setScale(2, RoundingMode.HALF_UP));

        // 3. Count of expenses
        long countThisMonth = expenseRepository.countByUserUserIdAndExpenseDateBetween(userId, startOfMonth, endOfMonth);
        response.setExpensesCountThisMonth(countThisMonth);

        long lifetimeCount = expenseRepository.countByUserUserId(userId);
        response.setTotalExpensesCount(lifetimeCount);

        // 4. Highest expenses
        expenseRepository.findTopByUserUserIdAndExpenseDateBetweenOrderByAmountDesc(userId, startOfMonth, endOfMonth)
                .ifPresent(exp -> response.setHighestExpenseThisMonth(expenseService.toResponse(exp)));

        expenseRepository.findTopByUserUserIdOrderByAmountDesc(userId)
                .ifPresent(exp -> response.setHighestExpenseLifetime(expenseService.toResponse(exp)));

        // 5. Budget status for this month
        budgetService.getBudgetForMonth(userId, year, month)
                .ifPresent(response::setBudget);

        // 6. Category breakdown for this month
        List<Object[]> catRows = expenseRepository.sumByCategoryForMonth(userId, year, month);
        List<CategorySpendingSummary> categoryBreakdown = new ArrayList<>();
        for (Object[] row : catRows) {
            Integer catId = (Integer) row[0];
            String catName = (String) row[1];
            String icon = (String) row[2];
            String color = (String) row[3];
            BigDecimal amount = (BigDecimal) row[4];
            long expCount = ((Number) row[5]).longValue();

            double percentage = 0.0;
            if (totalThisMonth.signum() > 0 && amount != null) {
                percentage = amount.divide(totalThisMonth, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            categoryBreakdown.add(new CategorySpendingSummary(
                    catId, catName, icon, color,
                    amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                    Math.round(percentage * 10.0) / 10.0,
                    expCount
            ));
        }
        response.setCategoryBreakdown(categoryBreakdown);

        // 7. Recent transactions (up to 10)
        List<Expense> recent = expenseRepository.findTop10ByUserUserIdOrderByExpenseDateDescExpenseIdDesc(userId);
        response.setRecentExpenses(recent.stream()
                .map(expenseService::toResponse)
                .collect(Collectors.toList()));

        // 8. Monthly spending trends
        List<Object[]> trendRows = expenseRepository.getMonthlyTrends(userId);
        List<MonthlyTrendSummary> trends = new ArrayList<>();
        for (Object[] row : trendRows) {
            int tYear = ((Number) row[0]).intValue();
            int tMonth = ((Number) row[1]).intValue();
            BigDecimal tAmount = (BigDecimal) row[2];
            String tLabel = Month.of(tMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + tYear;
            trends.add(new MonthlyTrendSummary(
                    tYear, tMonth, tLabel,
                    tAmount != null ? tAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO
            ));
        }
        response.setMonthlyTrends(trends);

        return response;
    }
}
