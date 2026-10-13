package com.trackmywallet.repository;

import com.trackmywallet.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ExpenseRepository - Spring Data JPA repository for the 'expenses' table.
 *
 * Contains both auto-generated queries (from method names) and
 * custom JPQL queries using @Query for more complex lookups.
 *
 * JPQL (Java Persistence Query Language) looks like SQL but uses
 * Java class names and field names instead of table/column names.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    /**
     * Find all expenses for a user, sorted by date descending (newest first).
     */
    List<Expense> findByUserUserIdOrderByExpenseDateDesc(Integer userId);

    /**
     * Find expenses for a user within a date range.
     * Used for monthly summaries and reports.
     */
    List<Expense> findByUserUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses for a user in a specific category.
     */
    List<Expense> findByUserUserIdAndCategoryCategoryId(Integer userId, Integer categoryId);

    /**
     * Calculate the total amount spent by a user in a given month/year.
     * Uses JPQL @Query for a SUM aggregation.
     *
     * 'e' is an alias for the Expense entity. YEAR() and MONTH() are JPQL functions.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.userId = :userId " +
           "AND YEAR(e.expenseDate) = :year " +
           "AND MONTH(e.expenseDate) = :month")
    BigDecimal sumByUserAndMonth(@Param("userId") Integer userId,
                                 @Param("year")   Integer year,
                                 @Param("month")  Integer month);

    /**
     * Get total expenses grouped by category for a user in a date range.
     * Returns a list of Object[] arrays: [categoryName, totalAmount]
     * Used for the category breakdown chart (donut chart).
     */
    @Query("SELECT c.name, SUM(e.amount) FROM Expense e " +
           "JOIN e.category c " +
           "WHERE e.user.userId = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.name " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategoryForDateRange(@Param("userId")    Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate")   LocalDate endDate);

    /**
     * Search expenses by description keyword (case-insensitive).
     * LOWER() converts both sides to lowercase before comparing.
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.user.userId = :userId " +
           "AND LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> searchByDescription(@Param("userId")  Integer userId,
                                      @Param("keyword") String keyword);

    /**
     * Find a specific expense by ID and user ID (ownership security check).
     */
    Optional<Expense> findByExpenseIdAndUserUserId(Integer expenseId, Integer userId);

    /**
     * Check if any expenses are linked to a category (used before category deletion).
     */
    boolean existsByCategoryCategoryId(Integer categoryId);

    /**
     * Count total expenses recorded by a user.
     */
    long countByUserUserId(Integer userId);

    /**
     * Count expenses within a date range for a user.
     */
    long countByUserUserIdAndExpenseDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get top expense by amount for a user.
     */
    Optional<Expense> findTopByUserUserIdOrderByAmountDesc(Integer userId);

    /**
     * Get top expense by amount in a date range for a user.
     */
    Optional<Expense> findTopByUserUserIdAndExpenseDateBetweenOrderByAmountDesc(
            Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * Top 5 most recent expenses for the user.
     */
    List<Expense> findTop5ByUserUserIdOrderByExpenseDateDescExpenseIdDesc(Integer userId);

    /**
     * Top 10 most recent expenses for the user.
     */
    List<Expense> findTop10ByUserUserIdOrderByExpenseDateDescExpenseIdDesc(Integer userId);

    /**
     * Comprehensive filter for expenses with optional parameters.
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.user.userId = :userId " +
           "AND (:categoryId IS NULL OR e.category.categoryId = :categoryId) " +
           "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.expenseDate <= :endDate) " +
           "AND (:keyword IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY e.expenseDate DESC, e.expenseId DESC")
    List<Expense> filterExpenses(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword);

    /**
     * Sum expenses for a user within a custom date range.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.userId = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateRange(@Param("userId") Integer userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Lifetime total expenses for a user.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.userId = :userId")
    BigDecimal sumTotalByUser(@Param("userId") Integer userId);

    /**
     * Detailed category breakdown for a specific month.
     */
    @Query("SELECT c.categoryId, c.name, c.icon, c.color, COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e JOIN e.category c " +
           "WHERE e.user.userId = :userId " +
           "AND YEAR(e.expenseDate) = :year AND MONTH(e.expenseDate) = :month " +
           "GROUP BY c.categoryId, c.name, c.icon, c.color " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategoryForMonth(@Param("userId") Integer userId,
                                         @Param("year") Integer year,
                                         @Param("month") Integer month);

    /**
     * Detailed category breakdown for a date range.
     */
    @Query("SELECT c.categoryId, c.name, c.icon, c.color, COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e JOIN e.category c " +
           "WHERE e.user.userId = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.categoryId, c.name, c.icon, c.color " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumCategoryDetailsForDateRange(@Param("userId") Integer userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Monthly trend aggregation (year, month, total).
     */
    @Query("SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), SUM(e.amount) " +
           "FROM Expense e " +
           "WHERE e.user.userId = :userId " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate) ASC, MONTH(e.expenseDate) ASC")
    List<Object[]> getMonthlyTrends(@Param("userId") Integer userId);
}
