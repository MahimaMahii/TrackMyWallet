package com.trackmywallet.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest DTO.
 * Carries the username and password from the browser's login form.
 */
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // --- Getters and Setters ---
    public String getUsername()                { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPassword()                { return password; }
    public void   setPassword(String password) { this.password = password; }
}
