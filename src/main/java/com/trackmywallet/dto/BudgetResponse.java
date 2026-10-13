package com.trackmywallet.dto;

import java.math.BigDecimal;

/**
 * BudgetResponse - Returns budget details along with spent amount, remaining amount,
 * usage percentage, and threshold warnings.
 */
public class BudgetResponse {

    private Integer budgetId;
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double percentageUsed;
    private boolean isExceeded;
    private boolean isNearLimit;
    private String statusMessage;

    public BudgetResponse() {}

    public BudgetResponse(Integer budgetId, Integer year, Integer month, BigDecimal amount,
                          BigDecimal spentAmount, BigDecimal remainingAmount, double percentageUsed,
                          boolean isExceeded, boolean isNearLimit, String statusMessage) {
        this.budgetId = budgetId;
        this.year = year;
        this.month = month;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.percentageUsed = percentageUsed;
        this.isExceeded = isExceeded;
        this.isNearLimit = isNearLimit;
        this.statusMessage = statusMessage;
    }

    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isExceeded() {
        return isExceeded;
    }

    public void setExceeded(boolean exceeded) {
        isExceeded = exceeded;
    }

    public boolean isNearLimit() {
        return isNearLimit;
    }

    public void setNearLimit(boolean nearLimit) {
        isNearLimit = nearLimit;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
