package com.trackmywallet.config;

import com.trackmywallet.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * SessionInterceptor - Guards every /api/** endpoint except auth endpoints.
 *
 * Think of this like a security guard at the door:
 *   - Public routes (/api/auth/login, /api/auth/register) → let through without checking.
 *   - All other /api/** routes → check for a valid session (logged-in user).
 *   - If no valid session → return HTTP 401 Unauthorized.
 *
 * This interceptor is registered in WebConfig.java.
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * This method runs BEFORE the controller method is called.
     *
     * @return true  → continue to the controller
     * @return false → stop here (response already sent as 401)
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path   = request.getRequestURI();
        String method = request.getMethod();

        // Allow OPTIONS (preflight) and all /api/auth/* routes through without session check
        if ("OPTIONS".equalsIgnoreCase(method)
                || path.startsWith("/api/auth/")
                || !path.startsWith("/api/")) {
            return true;
        }

        // Check if user is logged in (session exists with userId attribute)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthController.SESSION_USER_ID) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please log in to continue.\",\"data\":null}"
            );
            return false;
        }

        return true;
    }
}
