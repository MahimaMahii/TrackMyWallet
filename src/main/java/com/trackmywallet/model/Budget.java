package com.trackmywallet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class for the 'budgets' table.
 * Stores one monthly budget per user per (year, month).
 *
 * Example: user 1 sets a budget of ₹10,000 for October 2026.
 *          -> budget_year=2026, budget_month=10, amount=10000.00
 */
@Entity
@Table(
    name = "budgets",
    uniqueConstraints = {
        // A user can only have ONE budget per year-month combination
        @UniqueConstraint(name = "uq_user_budget", columnNames = {"user_id", "budget_year", "budget_month"})
    }
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Integer budgetId;

    // ---- Many budgets belong to ONE user ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Budget year is required")
    @Min(value = 2000, message = "Year must be >= 2000")
    @Max(value = 2100, message = "Year must be <= 2100")
    @Column(name = "budget_year", nullable = false)
    private Integer budgetYear;

    @NotNull(message = "Budget month is required")
    @Min(value = 1,  message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(name = "budget_month", nullable = false)
    private Integer budgetMonth;

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Lifecycle Callback ----
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Constructors ----
    public Budget() {}

    public Budget(User user, Integer budgetYear, Integer budgetMonth, BigDecimal amount) {
        this.user        = user;
        this.budgetYear  = budgetYear;
        this.budgetMonth = budgetMonth;
        this.amount      = amount;
    }

    // ---- Getters and Setters ----
    public Integer getBudgetId()                  { return budgetId; }
    public void    setBudgetId(Integer budgetId)  { this.budgetId = budgetId; }

    public User getUser()             { return user; }
    public void setUser(User user)    { this.user = user; }

    public Integer getBudgetYear()                  { return budgetYear; }
    public void    setBudgetYear(Integer budgetYear){ this.budgetYear = budgetYear; }

    public Integer getBudgetMonth()                   { return budgetMonth; }
    public void    setBudgetMonth(Integer budgetMonth){ this.budgetMonth = budgetMonth; }

    public BigDecimal getAmount()                    { return amount; }
    public void       setAmount(BigDecimal amount)   { this.amount = amount; }

    public LocalDateTime getCreatedAt()              { return createdAt; }

    @Override
    public String toString() {
        return "Budget{budgetId=" + budgetId + ", year=" + budgetYear
               + ", month=" + budgetMonth + ", amount=" + amount + "}";
    }
}
