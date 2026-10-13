package com.trackmywallet.dto;

/**
 * ApiResponse - A standard wrapper for all JSON responses from the server.
 *
 * Every REST API response from TrackMyWallet will look like this in the browser:
 *
 * Success:  { "success": true,  "message": "Login successful", "data": { ... } }
 * Failure:  { "success": false, "message": "Invalid password",  "data": null   }
 *
 * Using a consistent response format makes it easy to handle responses in JavaScript.
 */
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;       // Generic type: can be a User, a List, etc.

    // --- Static factory methods (convenient shortcuts) ---

    /** Create a success response with data payload. */
    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data    = data;
        return r;
    }

    /** Create a success response without data. */
    public static <T> ApiResponse<T> ok(String message) {
        return ok(message, null);
    }

    /** Create an error/failure response. */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.data    = null;
        return r;
    }

    // --- Getters ---
    public boolean isSuccess()  { return success; }
    public String  getMessage() { return message; }
    public T       getData()    { return data; }
}
