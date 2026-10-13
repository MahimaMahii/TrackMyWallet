package com.trackmywallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CategoryRequest - Payload for creating or updating an expense category.
 */
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    private String name;

    @Size(max = 50, message = "Icon identifier cannot exceed 50 characters")
    private String icon = "🏷️";

    @Size(max = 10, message = "Color code cannot exceed 10 characters")
    private String color = "#6C63FF";

    public CategoryRequest() {}

    public CategoryRequest(String name, String icon, String color) {
        this.name = name;
        this.icon = icon != null && !icon.isBlank() ? icon : "🏷️";
        this.color = color != null && !color.isBlank() ? color : "#6C63FF";
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
}
