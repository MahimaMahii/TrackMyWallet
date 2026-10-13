package com.trackmywallet;

import com.trackmywallet.dto.*;
import com.trackmywallet.model.User;
import com.trackmywallet.repository.UserRepository;
import com.trackmywallet.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ExpenseAndCategoryServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    private UserSummary testUser1;
    private UserSummary testUser2;

    @BeforeEach
    void setUp() {
        String uniqueSuffix1 = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("user1_" + uniqueSuffix1);
        req1.setEmail("user1_" + uniqueSuffix1 + "@test.com");
        req1.setFullName("User One");
        req1.setPassword("password123");
        testUser1 = userService.register(req1);

        String uniqueSuffix2 = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("user2_" + uniqueSuffix2);
        req2.setEmail("user2_" + uniqueSuffix2 + "@test.com");
        req2.setFullName("User Two");
        req2.setPassword("password123");
        testUser2 = userService.register(req2);
    }

    @Test
    void testCategoriesCreatedOnRegistration() {
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isGreaterThanOrEqualTo(6);
    }

    @Test
    void testCreateAndPreventDuplicateCategory() {
        CategoryRequest req = new CategoryRequest("Books", "📖", "#10B981");
        CategoryResponse created = categoryService.createCategory(testUser1.getUserId(), req);
        assertThat(created.getName()).isEqualTo("Books");
        assertThat(created.getIcon()).isEqualTo("📖");

        // Duplicate name for same user must be rejected
        assertThatThrownBy(() -> categoryService.createCategory(testUser1.getUserId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testCreateExpenseAndFilter() {
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = categories.get(0).getCategoryId();

        ExpenseRequest expReq = new ExpenseRequest(catId, new BigDecimal("450.50"), "Team lunch", LocalDate.now());
        ExpenseResponse expense = expenseService.createExpense(testUser1.getUserId(), expReq);

        assertThat(expense.getExpenseId()).isNotNull();
        assertThat(expense.getAmount()).isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(expense.getDescription()).isEqualTo("Team lunch");

        // Filter by keyword
        List<ExpenseResponse> filtered = expenseService.getExpenses(testUser1.getUserId(), null, null, null, "lunch");
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getExpenseId()).isEqualTo(expense.getExpenseId());

        // Negative/zero amount rejected
        ExpenseRequest invalidReq = new ExpenseRequest(catId, new BigDecimal("-10.00"), "Invalid", LocalDate.now());
        assertThatThrownBy(() -> expenseService.createExpense(testUser1.getUserId(), invalidReq))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCrossUserIsolation() {
        List<CategoryResponse> user1Categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = user1Categories.get(0).getCategoryId();

        ExpenseRequest expReq = new ExpenseRequest(catId, new BigDecimal("200.00"), "User 1 Coffee", LocalDate.now());
        ExpenseResponse user1Expense = expenseService.createExpense(testUser1.getUserId(), expReq);

        // User 2 should NOT be able to view User 1's expense
        assertThatThrownBy(() -> expenseService.getExpenseById(testUser2.getUserId(), user1Expense.getExpenseId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("access denied");

        // User 2 should NOT be able to update User 1's expense
        ExpenseRequest updateReq = new ExpenseRequest(catId, new BigDecimal("300.00"), "Hacked", LocalDate.now());
        assertThatThrownBy(() -> expenseService.updateExpense(testUser2.getUserId(), user1Expense.getExpenseId(), updateReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("access denied");

        // User 2 should NOT be able to delete User 1's expense
        assertThatThrownBy(() -> expenseService.deleteExpense(testUser2.getUserId(), user1Expense.getExpenseId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("access denied");

        // User 2 should NOT be able to use User 1's category to create an expense
        ExpenseRequest user2Req = new ExpenseRequest(catId, new BigDecimal("100.00"), "Cross category", LocalDate.now());
        assertThatThrownBy(() -> expenseService.createExpense(testUser2.getUserId(), user2Req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to your account");
    }

    @Test
    void testBudgetManagementAndCalculations() {
        LocalDate now = LocalDate.now();
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = categories.get(0).getCategoryId();

        // Add 500 expense
        expenseService.createExpense(testUser1.getUserId(), new ExpenseRequest(catId, new BigDecimal("500.00"), "Groceries", now));

        // Set budget of 1000
        BudgetRequest budgetReq = new BudgetRequest(now.getYear(), now.getMonthValue(), new BigDecimal("1000.00"));
        BudgetResponse budget = budgetService.setOrUpdateBudget(testUser1.getUserId(), budgetReq);

        assertThat(budget.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(budget.getSpentAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(budget.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(budget.getPercentageUsed()).isEqualTo(50.0);
        assertThat(budget.isExceeded()).isFalse();

        // Add 600 expense -> total spent 1100 -> budget exceeded
        expenseService.createExpense(testUser1.getUserId(), new ExpenseRequest(catId, new BigDecimal("600.00"), "Gadget", now));
        BudgetResponse updatedBudget = budgetService.getCurrentMonthBudget(testUser1.getUserId());

        assertThat(updatedBudget.getSpentAmount()).isEqualByComparingTo(new BigDecimal("1100.00"));
        assertThat(updatedBudget.isExceeded()).isTrue();
    }

    @Test
    void testDashboardSummaryAndAnalytics() {
        LocalDate now = LocalDate.now();
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = categories.get(0).getCategoryId();

        expenseService.createExpense(testUser1.getUserId(), new ExpenseRequest(catId, new BigDecimal("150.00"), "Snack", now));
        expenseService.createExpense(testUser1.getUserId(), new ExpenseRequest(catId, new BigDecimal("850.00"), "Headphones", now));

        DashboardSummaryResponse dashboard = dashboardService.getDashboardSummary(
                testUser1.getUserId(), now.getYear(), now.getMonthValue());

        assertThat(dashboard.getTotalExpensesThisMonth()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(dashboard.getExpensesCountThisMonth()).isEqualTo(2);
        assertThat(dashboard.getHighestExpenseThisMonth()).isNotNull();
        assertThat(dashboard.getHighestExpenseThisMonth().getAmount()).isEqualByComparingTo(new BigDecimal("850.00"));
        assertThat(dashboard.getCategoryBreakdown()).isNotEmpty();
    }

    @Test
    void testReportSummaryAndCsvExport() {
        LocalDate now = LocalDate.now();
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = categories.get(0).getCategoryId();

        expenseService.createExpense(testUser1.getUserId(), new ExpenseRequest(catId, new BigDecimal("250.00"), "Dinner", now));

        ReportSummaryResponse report = reportService.getReport(
                testUser1.getUserId(), now.minusDays(7), now, null);

        assertThat(report.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(report.getTransactionCount()).isEqualTo(1);
        assertThat(report.getExpenses()).hasSize(1);

        String csv = reportService.generateCsv(testUser1.getUserId(), now.minusDays(7), now, null);
        assertThat(csv).contains("Expense ID,Date,Category,Amount (INR),Description");
        assertThat(csv).contains("Dinner");
        assertThat(csv).contains("250.00");
    }

    @Test
    void testUpdateAndDeleteExpense() {
        LocalDate now = LocalDate.now();
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(testUser1.getUserId());
        Integer catId = categories.get(0).getCategoryId();

        ExpenseResponse created = expenseService.createExpense(
                testUser1.getUserId(),
                new ExpenseRequest(catId, new BigDecimal("120.00"), "Snack", now)
        );

        ExpenseRequest updateReq = new ExpenseRequest(catId, new BigDecimal("150.00"), "Evening Snack", now);
        ExpenseResponse updated = expenseService.updateExpense(testUser1.getUserId(), created.getExpenseId(), updateReq);
        assertThat(updated.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(updated.getDescription()).isEqualTo("Evening Snack");

        expenseService.deleteExpense(testUser1.getUserId(), created.getExpenseId());
        assertThatThrownBy(() -> expenseService.getExpenseById(testUser1.getUserId(), created.getExpenseId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdateAndDeleteCategory() {
        CategoryRequest createReq = new CategoryRequest("Gym & Fitness", "🏋️", "#F59E0B");
        CategoryResponse created = categoryService.createCategory(testUser1.getUserId(), createReq);

        CategoryRequest updateReq = new CategoryRequest("Fitness Club", "💪", "#EF4444");
        CategoryResponse updated = categoryService.updateCategory(testUser1.getUserId(), created.getCategoryId(), updateReq);
        assertThat(updated.getName()).isEqualTo("Fitness Club");
        assertThat(updated.getIcon()).isEqualTo("💪");

        categoryService.deleteCategory(testUser1.getUserId(), created.getCategoryId());
        assertThatThrownBy(() -> categoryService.getCategoryById(testUser1.getUserId(), created.getCategoryId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDeleteCategoryWithExpensesFails() {
        CategoryRequest catReq = new CategoryRequest("Sub-Services", "📱", "#3B82F6");
        CategoryResponse createdCat = categoryService.createCategory(testUser1.getUserId(), catReq);

        expenseService.createExpense(
                testUser1.getUserId(),
                new ExpenseRequest(createdCat.getCategoryId(), new BigDecimal("99.00"), "Monthly App Sub", LocalDate.now())
        );

        assertThatThrownBy(() -> categoryService.deleteCategory(testUser1.getUserId(), createdCat.getCategoryId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has expenses linked to it");
    }

    @Test
    void testDeleteBudget() {
        LocalDate now = LocalDate.now();
        BudgetRequest budgetReq = new BudgetRequest(now.getYear(), now.getMonthValue(), new BigDecimal("5000.00"));
        BudgetResponse created = budgetService.setOrUpdateBudget(testUser1.getUserId(), budgetReq);
        assertThat(created.getBudgetId()).isNotNull();

        budgetService.deleteBudget(testUser1.getUserId(), created.getBudgetId());
        BudgetResponse current = budgetService.getCurrentMonthBudget(testUser1.getUserId());
        assertThat(current).isNull();
    }
}
