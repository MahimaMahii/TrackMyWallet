package com.trackmywallet.repository;

import com.trackmywallet.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BudgetRepository - Spring Data JPA repository for the 'budgets' table.
 *
 * Provides methods to find, create, and update monthly budgets per user.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    /**
     * Find all budgets belonging to a user (all months/years).
     */
    List<Budget> findByUserUserIdOrderByBudgetYearDescBudgetMonthDesc(Integer userId);

    /**
     * Find the budget for a specific user, year, and month.
     * Returns Optional.empty() if no budget is set for that month.
     *
     * This is used on the dashboard to display the current month's budget.
     */
    Optional<Budget> findByUserUserIdAndBudgetYearAndBudgetMonth(
            Integer userId, Integer budgetYear, Integer budgetMonth);

    /**
     * Check if a budget already exists for a user in a given month.
     * Used before creating a new budget to avoid duplicate entries.
     */
    boolean existsByUserUserIdAndBudgetYearAndBudgetMonth(
            Integer userId, Integer budgetYear, Integer budgetMonth);

    /**
     * Find a budget by budgetId and user ID (ownership security check).
     */
    Optional<Budget> findByBudgetIdAndUserUserId(Integer budgetId, Integer userId);
}
