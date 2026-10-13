package com.trackmywallet.dto;

import java.math.BigDecimal;

/**
 * CategorySpendingSummary - Category breakdown data for charts and dashboard cards.
 */
public class CategorySpendingSummary {

    private Integer categoryId;
    private String categoryName;
    private String icon;
    private String color;
    private BigDecimal totalAmount;
    private double percentage;
    private long expenseCount;

    public CategorySpendingSummary() {}

    public CategorySpendingSummary(Integer categoryId, String categoryName, String icon,
                                   String color, BigDecimal totalAmount, double percentage,
                                   long expenseCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.icon = icon;
        this.color = color;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.expenseCount = expenseCount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }
}
