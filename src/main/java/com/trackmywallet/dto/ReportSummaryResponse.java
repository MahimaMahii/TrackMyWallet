package com.trackmywallet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ReportSummaryResponse - Data structure for custom date-range financial reports.
 */
public class ReportSummaryResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private long transactionCount;
    private BigDecimal dailyAverage;
    private ExpenseResponse highestExpense;
    private List<CategorySpendingSummary> categoryBreakdown;
    private List<ExpenseResponse> expenses;

    public ReportSummaryResponse() {}

    public ReportSummaryResponse(LocalDate startDate, LocalDate endDate, BigDecimal totalAmount,
                                 long transactionCount, BigDecimal dailyAverage, ExpenseResponse highestExpense,
                                 List<CategorySpendingSummary> categoryBreakdown, List<ExpenseResponse> expenses) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.dailyAverage = dailyAverage;
        this.highestExpense = highestExpense;
        this.categoryBreakdown = categoryBreakdown;
        this.expenses = expenses;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getDailyAverage() {
        return dailyAverage;
    }

    public void setDailyAverage(BigDecimal dailyAverage) {
        this.dailyAverage = dailyAverage;
    }

    public ExpenseResponse getHighestExpense() {
        return highestExpense;
    }

    public void setHighestExpense(ExpenseResponse highestExpense) {
        this.highestExpense = highestExpense;
    }

    public List<CategorySpendingSummary> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpendingSummary> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public List<ExpenseResponse> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseResponse> expenses) {
        this.expenses = expenses;
    }
}
