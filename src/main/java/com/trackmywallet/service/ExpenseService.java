package com.trackmywallet.service;

import com.trackmywallet.dto.ExpenseRequest;
import com.trackmywallet.dto.ExpenseResponse;
import com.trackmywallet.model.Category;
import com.trackmywallet.model.Expense;
import com.trackmywallet.model.User;
import com.trackmywallet.repository.CategoryRepository;
import com.trackmywallet.repository.ExpenseRepository;
import com.trackmywallet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExpenseService - Business logic for expense operations.
 * Enforces authenticated user ownership on all reads, updates, and deletes.
 */
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new expense for the authenticated user.
     */
    @Transactional
    public ExpenseResponse createExpense(Integer userId, ExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Category category = categoryRepository.findByCategoryIdAndUserUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to your account."));

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Expense amount must be strictly greater than zero.");
        }

        if (request.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required.");
        }

        String description = request.getDescription() != null ? request.getDescription().trim() : null;

        Expense expense = new Expense(user, category, request.getAmount(), description, request.getExpenseDate());
        Expense saved = expenseRepository.save(expense);

        return toResponse(saved);
    }

    /**
     * Retrieve expenses for the user with optional filtering (category, date range, keyword).
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(Integer userId, Integer categoryId,
                                             LocalDate startDate, LocalDate endDate,
                                             String keyword) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<Expense> expenses = expenseRepository.filterExpenses(
                userId, categoryId, startDate, endDate, cleanKeyword);

        return expenses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific expense by ID for the user.
     */
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Integer userId, Integer expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndUserUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or access denied."));
        return toResponse(expense);
    }

    /**
     * Update an existing expense.
     */
    @Transactional
    public ExpenseResponse updateExpense(Integer userId, Integer expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findByExpenseIdAndUserUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or access denied."));

        Category category = categoryRepository.findByCategoryIdAndUserUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to your account."));

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Expense amount must be strictly greater than zero.");
        }

        if (request.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required.");
        }

        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        expense.setExpenseDate(request.getExpenseDate());

        Expense updated = expenseRepository.save(expense);
        return toResponse(updated);
    }

    /**
     * Delete an expense owned by the authenticated user.
     */
    @Transactional
    public void deleteExpense(Integer userId, Integer expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndUserUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found or access denied."));
        expenseRepository.delete(expense);
    }

    /**
     * Map Expense entity to safe ExpenseResponse DTO.
     */
    public ExpenseResponse toResponse(Expense expense) {
        Category cat = expense.getCategory();
        return new ExpenseResponse(
                expense.getExpenseId(),
                cat != null ? cat.getCategoryId() : null,
                cat != null ? cat.getName() : "Uncategorized",
                cat != null ? cat.getIcon() : "🏷️",
                cat != null ? cat.getColor() : "#6C63FF",
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getCreatedAt()
        );
    }
}
