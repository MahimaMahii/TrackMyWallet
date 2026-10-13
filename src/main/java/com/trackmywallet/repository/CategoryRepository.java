package com.trackmywallet.repository;

import com.trackmywallet.model.Category;
import com.trackmywallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository - Spring Data JPA repository for the 'categories' table.
 *
 * Provides CRUD operations plus custom queries for finding categories
 * that belong to a specific user.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Find all categories that belong to a specific user.
     * Used to populate the category dropdown in the expense form.
     * Spring generates: SELECT * FROM categories WHERE user_id = ?
     */
    List<Category> findByUser(User user);

    /**
     * Find all categories by user ID (without needing the full User object).
     */
    List<Category> findByUserUserId(Integer userId);

    /**
     * Find a category by its ID and user ID (enforces ownership security).
     */
    Optional<Category> findByCategoryIdAndUserUserId(Integer categoryId, Integer userId);

    /**
     * Check if a category name already exists for a user by user ID.
     */
    boolean existsByUserUserIdAndNameIgnoreCase(Integer userId, String name);

    /**
     * Check if another category with the same name exists for the user (for updates).
     */
    boolean existsByUserUserIdAndNameIgnoreCaseAndCategoryIdNot(Integer userId, String name, Integer categoryId);
}
