package com.trackmywallet.dto;

/**
 * UserSummary - A safe view of user data to send to the browser.
 *
 * We NEVER send the passwordHash to the browser, even in encrypted form.
 * This DTO contains only the fields that are safe to expose.
 */
public class UserSummary {

    private Integer userId;
    private String  username;
    private String  email;
    private String  fullName;

    public UserSummary() {}

    public UserSummary(Integer userId, String username, String email, String fullName) {
        this.userId   = userId;
        this.username = username;
        this.email    = email;
        this.fullName = fullName;
    }

    // --- Getters ---
    public Integer getUserId()   { return userId; }
    public String  getUsername() { return username; }
    public String  getEmail()    { return email; }
    public String  getFullName() { return fullName; }
}
