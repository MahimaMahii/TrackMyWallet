package com.trackmywallet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class for the 'users' table.
 *
 * @Entity   - tells Spring/JPA that this Java class maps to a database table.
 * @Table    - specifies which table name in MySQL to map to.
 * Each field annotated with @Column maps to a specific column.
 */
@Entity
@Table(name = "users")
public class User {

    // ---- Primary Key ----
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT in MySQL
    @Column(name = "user_id")
    private Integer userId;

    // ---- Columns ----
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // IMPORTANT: This field stores a BCrypt HASH, never the real password
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Relationships ----
    // One user can have many categories (OneToMany)
    // mappedBy = "user" refers to the 'user' field inside Category.java
    // cascade = ALL means: if we delete a user, delete their categories too
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    // ---- Lifecycle Callback ----
    // @PrePersist runs automatically right before inserting a new row into DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Constructors ----
    public User() {}

    public User(String username, String email, String passwordHash, String fullName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    // ---- Getters and Setters ----
    public Integer getUserId()                  { return userId; }
    public void   setUserId(Integer userId)     { this.userId = userId; }

    public String getUsername()                 { return username; }
    public void   setUsername(String username)  { this.username = username; }

    public String getEmail()                    { return email; }
    public void   setEmail(String email)        { this.email = email; }

    public String getPasswordHash()             { return passwordHash; }
    public void   setPasswordHash(String h)     { this.passwordHash = h; }

    public String getFullName()                 { return fullName; }
    public void   setFullName(String fullName)  { this.fullName = fullName; }

    public LocalDateTime getCreatedAt()         { return createdAt; }

    public List<Category> getCategories()       { return categories; }
    public List<Expense>  getExpenses()         { return expenses; }
    public List<Budget>   getBudgets()          { return budgets; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', email='" + email + "'}";
    }
}
