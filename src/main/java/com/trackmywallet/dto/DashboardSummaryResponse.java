package com.trackmywallet.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DashboardSummaryResponse - Aggregate financial summary for the dashboard.
 */
public class DashboardSummaryResponse {

    private int year;
    private int month;
    private String periodLabel;

    private BigDecimal totalExpensesThisMonth;
    private BigDecimal totalExpensesLifetime;
    private long expensesCountThisMonth;
    private long totalExpensesCount;

    private ExpenseResponse highestExpenseThisMonth;
    private ExpenseResponse highestExpenseLifetime;

    private BudgetResponse budget;

    private List<CategorySpendingSummary> categoryBreakdown;
    private List<ExpenseResponse> recentExpenses;
    private List<MonthlyTrendSummary> monthlyTrends;

    public DashboardSummaryResponse() {}

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getTotalExpensesThisMonth() {
        return totalExpensesThisMonth;
    }

    public void setTotalExpensesThisMonth(BigDecimal totalExpensesThisMonth) {
        this.totalExpensesThisMonth = totalExpensesThisMonth;
    }

    public BigDecimal getTotalExpensesLifetime() {
        return totalExpensesLifetime;
    }

    public void setTotalExpensesLifetime(BigDecimal totalExpensesLifetime) {
        this.totalExpensesLifetime = totalExpensesLifetime;
    }

    public long getExpensesCountThisMonth() {
        return expensesCountThisMonth;
    }

    public void setExpensesCountThisMonth(long expensesCountThisMonth) {
        this.expensesCountThisMonth = expensesCountThisMonth;
    }

    public long getTotalExpensesCount() {
        return totalExpensesCount;
    }

    public void setTotalExpensesCount(long totalExpensesCount) {
        this.totalExpensesCount = totalExpensesCount;
    }

    public ExpenseResponse getHighestExpenseThisMonth() {
        return highestExpenseThisMonth;
    }

    public void setHighestExpenseThisMonth(ExpenseResponse highestExpenseThisMonth) {
        this.highestExpenseThisMonth = highestExpenseThisMonth;
    }

    public ExpenseResponse getHighestExpenseLifetime() {
        return highestExpenseLifetime;
    }

    public void setHighestExpenseLifetime(ExpenseResponse highestExpenseLifetime) {
        this.highestExpenseLifetime = highestExpenseLifetime;
    }

    public BudgetResponse getBudget() {
        return budget;
    }

    public void setBudget(BudgetResponse budget) {
        this.budget = budget;
    }

    public List<CategorySpendingSummary> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpendingSummary> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public List<ExpenseResponse> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<ExpenseResponse> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }

    public List<MonthlyTrendSummary> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<MonthlyTrendSummary> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
}
