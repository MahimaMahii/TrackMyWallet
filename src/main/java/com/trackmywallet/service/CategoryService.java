package com.trackmywallet.service;

import com.trackmywallet.dto.CategoryRequest;
import com.trackmywallet.dto.CategoryResponse;
import com.trackmywallet.model.Category;
import com.trackmywallet.model.User;
import com.trackmywallet.repository.CategoryRepository;
import com.trackmywallet.repository.ExpenseRepository;
import com.trackmywallet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CategoryService - Handles category listing, creation, updates, and deletion.
 * All operations are strictly scoped to the authenticated user.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieve all categories belonging to the user.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesForUser(Integer userId) {
        List<Category> categories = categoryRepository.findByUserUserId(userId);
        return categories.stream().map(c -> {
            long count = expenseRepository.findByUserUserIdAndCategoryCategoryId(userId, c.getCategoryId()).size();
            return toResponse(c, count);
        }).collect(Collectors.toList());
    }

    /**
     * Retrieve a specific category by ID for the user.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer userId, Integer categoryId) {
        Category category = categoryRepository.findByCategoryIdAndUserUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to you."));
        long count = expenseRepository.findByUserUserIdAndCategoryCategoryId(userId, categoryId).size();
        return toResponse(category, count);
    }

    /**
     * Create a new category for the authenticated user.
     */
    @Transactional
    public CategoryResponse createCategory(Integer userId, CategoryRequest request) {
        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByUserUserIdAndNameIgnoreCase(userId, trimmedName)) {
            throw new IllegalArgumentException("A category named '" + trimmedName + "' already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String icon = request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon().trim() : "🏷️";
        String color = request.getColor() != null && !request.getColor().isBlank() ? request.getColor().trim() : "#6C63FF";

        Category category = new Category(user, trimmedName, icon, color);
        Category saved = categoryRepository.save(category);

        return toResponse(saved, 0);
    }

    /**
     * Update an existing category.
     */
    @Transactional
    public CategoryResponse updateCategory(Integer userId, Integer categoryId, CategoryRequest request) {
        Category category = categoryRepository.findByCategoryIdAndUserUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to you."));

        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByUserUserIdAndNameIgnoreCaseAndCategoryIdNot(userId, trimmedName, categoryId)) {
            throw new IllegalArgumentException("Another category named '" + trimmedName + "' already exists.");
        }

        category.setName(trimmedName);
        if (request.getIcon() != null && !request.getIcon().isBlank()) {
            category.setIcon(request.getIcon().trim());
        }
        if (request.getColor() != null && !request.getColor().isBlank()) {
            category.setColor(request.getColor().trim());
        }

        Category updated = categoryRepository.save(category);
        long count = expenseRepository.findByUserUserIdAndCategoryCategoryId(userId, categoryId).size();
        return toResponse(updated, count);
    }

    /**
     * Delete an existing category.
     * Prevents deletion if expenses are associated with this category.
     */
    @Transactional
    public void deleteCategory(Integer userId, Integer categoryId) {
        Category category = categoryRepository.findByCategoryIdAndUserUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to you."));

        if (expenseRepository.existsByCategoryCategoryId(categoryId)) {
            throw new IllegalStateException("Cannot delete category '" + category.getName()
                    + "' because it has expenses linked to it. Please delete or reassign those expenses first.");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category, long expenseCount) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                expenseCount
        );
    }
}
