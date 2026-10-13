package com.trackmywallet.dto;

/**
 * CategoryResponse - Data returned to the client when querying categories.
 */
public class CategoryResponse {

    private Integer categoryId;
    private String name;
    private String icon;
    private String color;
    private long expenseCount;

    public CategoryResponse() {}

    public CategoryResponse(Integer categoryId, String name, String icon, String color, long expenseCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.expenseCount = expenseCount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }
}
