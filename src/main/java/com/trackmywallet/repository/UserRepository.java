package com.trackmywallet.repository;

import com.trackmywallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Spring Data JPA repository for the 'users' table.
 *
 * By extending JpaRepository<User, Integer>, Spring automatically gives us:
 *   - save(user)          -> INSERT or UPDATE
 *   - findById(id)        -> SELECT by primary key
 *   - findAll()           -> SELECT all users
 *   - delete(user)        -> DELETE
 *   - count()             -> COUNT(*)
 *
 * We add custom finder methods below using Spring's "method name" convention.
 * Spring reads the method name and generates the SQL query automatically!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find a user by their username.
     * Spring generates: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     * Spring generates: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists (for registration validation).
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists (for registration validation).
     */
    boolean existsByEmail(String email);
}
