package com.trackmywallet.service;

import com.trackmywallet.dto.CategorySpendingSummary;
import com.trackmywallet.dto.ExpenseResponse;
import com.trackmywallet.dto.ReportSummaryResponse;
import com.trackmywallet.model.Expense;
import com.trackmywallet.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportService - Generates date-range financial analysis and CSV export data.
 */
@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    public ReportService(ExpenseRepository expenseRepository, ExpenseService expenseService) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    /**
     * Generate report summary for a specific date range and optional category filter.
     */
    @Transactional(readOnly = true)
    public ReportSummaryResponse getReport(Integer userId, LocalDate startDate, LocalDate endDate, Integer categoryId) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.withDayOfMonth(1);

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<Expense> expenses = expenseRepository.filterExpenses(userId, categoryId, start, end, null);
        List<ExpenseResponse> expenseResponses = expenses.stream()
                .map(expenseService::toResponse)
                .collect(Collectors.toList());

        BigDecimal total = expenseRepository.sumByUserAndDateRange(userId, start, end);
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        long count = expenses.size();
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        BigDecimal dailyAvg = total.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

        ExpenseResponse highestExpense = null;
        if (!expenses.isEmpty()) {
            Expense highest = expenses.stream()
                    .max((a, b) -> a.getAmount().compareTo(b.getAmount()))
                    .orElse(null);
            if (highest != null) {
                highestExpense = expenseService.toResponse(highest);
            }
        }

        List<Object[]> catRows = expenseRepository.sumCategoryDetailsForDateRange(userId, start, end);
        List<CategorySpendingSummary> categoryBreakdown = new ArrayList<>();
        for (Object[] row : catRows) {
            Integer catId = (Integer) row[0];
            String catName = (String) row[1];
            String icon = (String) row[2];
            String color = (String) row[3];
            BigDecimal amount = (BigDecimal) row[4];
            long expCount = ((Number) row[5]).longValue();

            double percentage = 0.0;
            if (total.signum() > 0 && amount != null) {
                percentage = amount.divide(total, 4, RoundingMode.HALF_UP)
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

        return new ReportSummaryResponse(
                start,
                end,
                total.setScale(2, RoundingMode.HALF_UP),
                count,
                dailyAvg,
                highestExpense,
                categoryBreakdown,
                expenseResponses
        );
    }

    /**
     * Generate CSV content for expenses within the specified filter.
     */
    @Transactional(readOnly = true)
    public String generateCsv(Integer userId, LocalDate startDate, LocalDate endDate, Integer categoryId) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusMonths(3);

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<Expense> expenses = expenseRepository.filterExpenses(userId, categoryId, start, end, null);

        StringBuilder sb = new StringBuilder();
        sb.append("Expense ID,Date,Category,Amount (INR),Description\n");

        for (Expense exp : expenses) {
            String catName = exp.getCategory() != null ? exp.getCategory().getName() : "Uncategorized";
            String desc = exp.getDescription() != null ? exp.getDescription() : "";
            // Escape CSV cell (RFC 4180)
            String safeDesc = "\"" + desc.replace("\"", "\"\"") + "\"";
            String safeCat = "\"" + catName.replace("\"", "\"\"") + "\"";

            sb.append(exp.getExpenseId()).append(",")
              .append(exp.getExpenseDate()).append(",")
              .append(safeCat).append(",")
              .append(exp.getAmount()).append(",")
              .append(safeDesc).append("\n");
        }

        return sb.toString();
    }
}
