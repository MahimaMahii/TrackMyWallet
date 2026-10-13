package com.trackmywallet.service;

import com.trackmywallet.dto.RegisterRequest;
import com.trackmywallet.dto.UserSummary;
import com.trackmywallet.model.Category;
import com.trackmywallet.model.User;
import com.trackmywallet.repository.CategoryRepository;
import com.trackmywallet.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * UserService - Business logic layer for user registration and login.
 *
 * @Service  tells Spring to create one instance of this class and manage it.
 *           It can then be injected anywhere using @Autowired.
 *
 * @Transactional means: "wrap the method in a database transaction".
 *           If anything goes wrong midway, all database changes are rolled back.
 */
@Service
public class UserService {

    // BCryptPasswordEncoder performs one-way hashing.
    // The '12' is the "strength" (cost factor). Higher = slower to crack but also slower to hash.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // Spring injects these repositories automatically (constructor injection)
    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;

    public UserService(UserRepository userRepository, CategoryRepository categoryRepository) {
        this.userRepository     = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // =========================================================================
    //  REGISTER
    // =========================================================================

    /**
     * Register a new user.
     *
     * Steps:
     *   1. Check username/email are not already taken
     *   2. Hash the plain-text password using BCrypt
     *   3. Save the new user to the database
     *   4. Create default expense categories for the new user
     *   5. Return a safe UserSummary (no password hash exposed)
     *
     * @throws IllegalArgumentException if username or email is already taken
     */
    @Transactional
    public UserSummary register(RegisterRequest request) {

        // Step 1: Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered.");
        }

        // Step 2: Hash the password — NEVER store plain text!
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Step 3: Build the User entity and save it
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            hashedPassword,
            request.getFullName()
        );
        user = userRepository.save(user);

        // Step 4: Create sensible default categories for new users
        createDefaultCategories(user);

        // Step 5: Return safe view (no password hash)
        return toSummary(user);
    }

    // =========================================================================
    //  LOGIN
    // =========================================================================

    /**
     * Validate login credentials.
     *
     * @param username plain-text username from the form
     * @param password plain-text password from the form
     * @return UserSummary if credentials are valid
     * @throws IllegalArgumentException if username not found or password wrong
     */
    public UserSummary login(String username, String password) {

        // Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("No account found with that username."));

        // passwordEncoder.matches() compares plain-text to the stored BCrypt hash
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }

        return toSummary(user);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    /** Convert a User entity to a safe UserSummary DTO. */
    public UserSummary toSummary(User user) {
        return new UserSummary(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName()
        );
    }

    /** Find a user by ID (used by other services and interceptors). */
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * Create a standard set of expense categories for every new user.
     * Users can add more or rename these later.
     */
    @Transactional
    public void createDefaultCategories(User user) {
        List<String[]> defaults = List.of(
            // { name, icon, color }
            new String[]{"Food & Dining",    "🍽️",  "#FF6B6B"},
            new String[]{"Transportation",   "🚗",  "#4ECDC4"},
            new String[]{"Shopping",         "🛍️",  "#A855F7"},
            new String[]{"Entertainment",    "🎬",  "#F59E0B"},
            new String[]{"Health & Medical", "🏥",  "#10B981"},
            new String[]{"Utilities",        "💡",  "#3B82F6"},
            new String[]{"Education",        "📚",  "#6366F1"},
            new String[]{"Travel",           "✈️",  "#EC4899"},
            new String[]{"Groceries",        "🛒",  "#84CC16"},
            new String[]{"Others",           "💰",  "#94A3B8"}
        );

        for (String[] cat : defaults) {
            Category category = new Category(user, cat[0], cat[1], cat[2]);
            categoryRepository.save(category);
        }
    }
}
