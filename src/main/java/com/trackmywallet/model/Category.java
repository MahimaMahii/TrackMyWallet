package com.trackmywallet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the 'categories' table.
 * Each category belongs to one user (ManyToOne relationship).
 */
@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        // Ensures a user cannot have two categories with the same name
        @UniqueConstraint(name = "uq_user_category", columnNames = {"user_id", "name"})
    }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    // ---- Many categories belong to ONE user ----
    // @ManyToOne  = many categories -> one user
    // @JoinColumn = the foreign key column in the 'categories' table is 'user_id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Category name is required")
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "icon", length = 50)
    private String icon = "tag";

    @Column(name = "color", length = 10)
    private String color = "#6C63FF";

    // ---- A category can have many expenses ----
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    // ---- Constructors ----
    public Category() {}

    public Category(User user, String name, String icon, String color) {
        this.user  = user;
        this.name  = name;
        this.icon  = icon;
        this.color = color;
    }

    // ---- Getters and Setters ----
    public Integer getCategoryId()                    { return categoryId; }
    public void    setCategoryId(Integer categoryId)  { this.categoryId = categoryId; }

    public User   getUser()             { return user; }
    public void   setUser(User user)    { this.user = user; }

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    public String getIcon()             { return icon; }
    public void   setIcon(String icon)  { this.icon = icon; }

    public String getColor()              { return color; }
    public void   setColor(String color)  { this.color = color; }

    public List<Expense> getExpenses()    { return expenses; }

    @Override
    public String toString() {
        return "Category{categoryId=" + categoryId + ", name='" + name + "'}";
    }
}
