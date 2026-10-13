package com.trackmywallet.dto;

import java.math.BigDecimal;

/**
 * MonthlyTrendSummary - Monthly spending trend data point for time-series charts.
 */
public class MonthlyTrendSummary {

    private Integer year;
    private Integer month;
    private String monthLabel;
    private BigDecimal totalAmount;

    public MonthlyTrendSummary() {}

    public MonthlyTrendSummary(Integer year, Integer month, String monthLabel, BigDecimal totalAmount) {
        this.year = year;
        this.month = month;
        this.monthLabel = monthLabel;
        this.totalAmount = totalAmount;
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

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
