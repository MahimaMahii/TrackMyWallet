package com.trackmywallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest DTO (Data Transfer Object).
 *
 * A DTO is a simple container that carries data from the browser (JSON request body)
 * into our Java controller. It is NOT stored in the database directly —
 * the Service layer converts it into a User entity first.
 */
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // --- Getters and Setters ---
    public String getFullName()                   { return fullName; }
    public void   setFullName(String fullName)    { this.fullName = fullName; }

    public String getUsername()                   { return username; }
    public void   setUsername(String username)    { this.username = username; }

    public String getEmail()                      { return email; }
    public void   setEmail(String email)          { this.email = email; }

    public String getPassword()                   { return password; }
    public void   setPassword(String password)    { this.password = password; }
}
