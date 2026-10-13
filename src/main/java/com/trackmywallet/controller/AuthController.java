package com.trackmywallet.controller;

import com.trackmywallet.dto.ApiResponse;
import com.trackmywallet.dto.LoginRequest;
import com.trackmywallet.dto.RegisterRequest;
import com.trackmywallet.dto.UserSummary;
import com.trackmywallet.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for user authentication.
 *
 * @RestController  = @Controller + @ResponseBody.
 *   Every method returns JSON automatically (Spring serializes to JSON using Jackson).
 *
 * @RequestMapping  sets the base URL prefix. All endpoints here start with /api/auth/
 *
 * Session Management:
 *   We use the standard Java HTTP Session to track logged-in users.
 *   On login:  store userId in the session → browser gets a session cookie (JSESSIONID).
 *   On logout: invalidate the session     → session cookie is deleted.
 *   Other APIs check for SESSION_USER_ID to verify the user is logged in.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // The key name used to store userId in the HTTP session
    public static final String SESSION_USER_ID   = "userId";
    public static final String SESSION_USERNAME  = "username";
    public static final String SESSION_FULL_NAME = "fullName";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================================
    //  POST /api/auth/register
    //  Register a new user account.
    // =========================================================================

    /**
     * @Valid triggers the validation annotations on RegisterRequest.
     * If any field fails validation, Spring returns a 400 error automatically.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserSummary>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        try {
            UserSummary user = userService.register(request);

            // Log the user in automatically right after registration
            createSession(httpRequest, user);

            return ResponseEntity.ok(ApiResponse.ok("Account created successfully! Welcome, " + user.getFullName() + "!", user));

        } catch (IllegalArgumentException e) {
            // Username or email already taken
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }

    // =========================================================================
    //  POST /api/auth/login
    //  Log in with username and password.
    // =========================================================================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserSummary>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            UserSummary user = userService.login(request.getUsername(), request.getPassword());

            // Create a session so the browser is recognized on future requests
            createSession(httpRequest, user);

            return ResponseEntity.ok(ApiResponse.ok("Welcome back, " + user.getFullName() + "!", user));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Login failed. Please try again."));
        }
    }

    // =========================================================================
    //  POST /api/auth/logout
    //  Log out and destroy the session.
    // =========================================================================

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);  // false = don't create a new one
        if (session != null) {
            session.invalidate();  // Clears all session data and invalidates the cookie
        }
        return ResponseEntity.ok(ApiResponse.ok("You have been logged out successfully."));
    }

    // =========================================================================
    //  GET /api/auth/me
    //  Get the currently logged-in user's info.
    // =========================================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummary>> getCurrentUser(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Not logged in. Please log in first."));
        }

        Integer userId   = (Integer) session.getAttribute(SESSION_USER_ID);
        String  username = (String)  session.getAttribute(SESSION_USERNAME);
        String  fullName = (String)  session.getAttribute(SESSION_FULL_NAME);

        // We can fetch from DB for fresh data, or use session values for speed
        UserSummary summary = userService.findById(userId)
            .map(userService::toSummary)
            .orElse(null);

        if (summary == null) {
            session.invalidate();
            return ResponseEntity.status(401).body(ApiResponse.error("Session expired. Please log in again."));
        }

        return ResponseEntity.ok(ApiResponse.ok("Authenticated", summary));
    }

    // =========================================================================
    //  PRIVATE HELPERS
    // =========================================================================

    /** Store key user info in the HTTP session after successful login/register. */
    private void createSession(HttpServletRequest httpRequest, UserSummary user) {
        // getSession(true) = create a new session if one doesn't exist
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SESSION_USER_ID,   user.getUserId());
        session.setAttribute(SESSION_USERNAME,  user.getUsername());
        session.setAttribute(SESSION_FULL_NAME, user.getFullName());
        // Session timeout is configured in application.properties (30 minutes)
    }
}
