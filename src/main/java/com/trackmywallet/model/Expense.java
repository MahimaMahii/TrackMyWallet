package com.trackmywallet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class for the 'expenses' table.
 * Each expense belongs to one user and one category.
 *
 * BigDecimal is used for monetary amounts to avoid floating-point rounding errors.
 * LocalDate  is used for the expense date (no time part needed).
 */
@Entity
@Table(
    name = "expenses",
    indexes = {
        @Index(name = "idx_expenses_user_date", columnList = "user_id, expense_date")
    }
)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Integer expenseId;

    // ---- Many expenses belong to ONE user ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ---- Many expenses belong to ONE category ----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // BigDecimal for precise money arithmetic (precision=10, scale=2 -> max 99999999.99)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Lifecycle Callback ----
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Constructors ----
    public Expense() {}

    public Expense(User user, Category category, BigDecimal amount,
                   String description, LocalDate expenseDate) {
        this.user        = user;
        this.category    = category;
        this.amount      = amount;
        this.description = description;
        this.expenseDate = expenseDate;
    }

    // ---- Getters and Setters ----
    public Integer getExpenseId()                    { return expenseId; }
    public void    setExpenseId(Integer expenseId)   { this.expenseId = expenseId; }

    public User     getUser()               { return user; }
    public void     setUser(User user)      { this.user = user; }

    public Category getCategory()                   { return category; }
    public void     setCategory(Category category)  { this.category = category; }

    public BigDecimal getAmount()                    { return amount; }
    public void       setAmount(BigDecimal amount)   { this.amount = amount; }

    public String getDescription()                       { return description; }
    public void   setDescription(String description)     { this.description = description; }

    public LocalDate getExpenseDate()                    { return expenseDate; }
    public void      setExpenseDate(LocalDate expDate)   { this.expenseDate = expDate; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }

    @Override
    public String toString() {
        return "Expense{expenseId=" + expenseId + ", amount=" + amount
               + ", date=" + expenseDate + "}";
    }
}
