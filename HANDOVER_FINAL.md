# 🏆 TrackMyWallet — Final Project Handover & Documentation

**Date:** October 13, 2026  
**Project:** TrackMyWallet — Personal Expense Tracking & Budget Management Web Application  
**Final Status:** 🟢 **ALL STAGES (1 to 8) COMPLETED & 100% VERIFIED**  
**Automated Tests:** 15 / 15 Passed (0 Failures, 0 Errors, 0 Skipped)  

---

## 📌 Executive Summary

TrackMyWallet is a full-stack personal finance web application built with Spring Boot, Hibernate, MySQL, and a responsive glassmorphic dark-theme frontend.

All stages from Stage 1 through Stage 8 have been implemented end-to-end, tested against a live MySQL database, and verified:
- **Stage 1:** Project Setup & Core Configuration
- **Stage 2:** Database Architecture & JPA Entities (`users`, `categories`, `expenses`, `budgets`)
- **Stage 3:** BCrypt Authentication, Session Security & `login.html`
- **Stage 4:** Expense and Category Management (CRUD, validations, user isolation)
- **Stage 5:** Dashboard Overview & Financial Summary Metrics
- **Stage 6:** Monthly Budget Management & Spending Alerts
- **Stage 7:** Date-Range Reports, Analytics & CSV Export
- **Stage 8:** Complete Responsive Frontend Integration (`dashboard.html`, `dashboard.css`, `dashboard.js`)

---

## 🛠️ Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Runtime & Language** | Java 17 / Java 21 | Modern LTS Java |
| **Backend Framework** | Spring Boot 4.1.1 | Spring MVC, Spring Data JPA |
| **ORM & Persistence** | Hibernate 7 (7.4.5.Final) | Connection pool HikariCP |
| **Database** | MySQL 8.0 | `localhost:3306`, database `trackmywallet` |
| **Security & Auth** | `spring-security-crypto` + Servlet Session | BCrypt hashing, HTTP Session (`JSESSIONID`) |
| **Frontend UI** | HTML5, CSS3, Vanilla JavaScript | Glassmorphic dark design, Chart.js 4.4 |
| **Build & Tooling** | Maven Wrapper | `.\mvnw.cmd` |

---

## 📂 Project Architecture & Package Structure

```
com.trackmywallet/
├── TrackMyWalletApplication.java     # Main Spring Boot Entry Point
├── config/
│   ├── SessionInterceptor.java       # Guards /api/**, rejects unauthenticated calls (401)
│   └── WebConfig.java                # Registers Interceptor & static resource handlers
├── controller/
│   ├── AuthController.java           # /api/auth (register, login, logout, me)
│   ├── CategoryController.java       # /api/categories (CRUD user categories)
│   ├── ExpenseController.java        # /api/expenses (CRUD user expenses & filters)
│   ├── BudgetController.java         # /api/budgets (set, update, fetch user budgets)
│   ├── DashboardController.java      # /api/dashboard/summary (live aggregated stats)
│   └── ReportController.java         # /api/reports (summary & CSV download)
├── dto/
│   ├── ApiResponse.java              # Standardized API response envelope
│   ├── RegisterRequest.java          # Validation for new user registration
│   ├── LoginRequest.java             # Credentials payload
│   ├── UserSummary.java              # Safe user DTO (no password hash)
│   ├── CategoryRequest.java          # Category create/update payload
│   ├── CategoryResponse.java         # Category output DTO with expense count
│   ├── ExpenseRequest.java           # Expense create/update payload
│   ├── ExpenseResponse.java          # Full expense output DTO with category details
│   ├── BudgetRequest.java            # Budget payload (year, month, amount)
│   ├── BudgetResponse.java           # Budget analysis (spent, remaining, %, alert flags)
│   ├── CategorySpendingSummary.java  # Category spending breakdown & percentage
│   ├── MonthlyTrendSummary.java      # Month-by-month spending trends
│   ├── DashboardSummaryResponse.java # Aggregated overview response
│   └── ReportSummaryResponse.java    # Date-range report aggregation response
├── model/
│   ├── User.java                     # Maps to 'users' table
│   ├── Category.java                 # Maps to 'categories' table (User FK)
│   ├── Expense.java                  # Maps to 'expenses' table (User & Category FK)
│   └── Budget.java                   # Maps to 'budgets' table (User FK, unique year+month)
├── repository/
│   ├── UserRepository.java           # User entity data access
│   ├── CategoryRepository.java       # Category queries with user isolation
│   ├── ExpenseRepository.java        # Advanced JPQL queries for aggregations and filtering
│   └── BudgetRepository.java         # Monthly budget lookup by user and period
└── service/
    ├── UserService.java              # BCrypt hashing, default category provisioning
    ├── CategoryService.java          # Category business rules and relational constraints
    ├── ExpenseService.java           # Expense CRUD, strict user ownership enforcement
    ├── BudgetService.java            # Budget calculation engine, alerts, limit tracking
    ├── DashboardService.java         # Assembles real-time financial stats & charts data
    └── ReportService.java            # Date-range reporting & RFC 4180 CSV generation
```

---

## 🗄️ Database Schema & Tables

The schema resides in `sql/trackmywallet_schema.sql` and is managed with `spring.jpa.hibernate.ddl-auto=update`:

1. **`users`**
   - `user_id` INT AUTO_INCREMENT PRIMARY KEY
   - `username` VARCHAR(50) UNIQUE NOT NULL
   - `email` VARCHAR(100) UNIQUE NOT NULL
   - `password_hash` VARCHAR(255) NOT NULL (BCrypt hashed)
   - `full_name` VARCHAR(100) NOT NULL
   - `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

2. **`categories`**
   - `category_id` INT AUTO_INCREMENT PRIMARY KEY
   - `user_id` INT NOT NULL (FK -> `users.user_id` ON DELETE CASCADE)
   - `name` VARCHAR(50) NOT NULL
   - `icon` VARCHAR(50) DEFAULT 'tag'
   - `color` VARCHAR(10) DEFAULT '#6C63FF'
   - `UNIQUE KEY uq_user_category (user_id, name)`

3. **`expenses`**
   - `expense_id` INT AUTO_INCREMENT PRIMARY KEY
   - `user_id` INT NOT NULL (FK -> `users.user_id` ON DELETE CASCADE)
   - `category_id` INT NOT NULL (FK -> `categories.category_id` ON DELETE RESTRICT)
   - `amount` DECIMAL(10,2) NOT NULL CHECK (amount > 0)
   - `description` VARCHAR(200) DEFAULT NULL
   - `expense_date` DATE NOT NULL
   - `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   - `INDEX idx_expenses_user_date (user_id, expense_date)`

4. **`budgets`**
   - `budget_id` INT AUTO_INCREMENT PRIMARY KEY
   - `user_id` INT NOT NULL (FK -> `users.user_id` ON DELETE CASCADE)
   - `budget_year` INT NOT NULL
   - `budget_month` INT NOT NULL CHECK (budget_month BETWEEN 1 AND 12)
   - `amount` DECIMAL(10,2) NOT NULL CHECK (amount > 0)
   - `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   - `UNIQUE KEY uq_user_budget (user_id, budget_year, budget_month)`

---

## 🌐 REST API Specification

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user + auto-seed 6 categories | No |
| `POST` | `/api/auth/login` | Authenticate with username & password | No |
| `POST` | `/api/auth/logout` | Destroy session & invalidate cookie | Yes |
| `GET` | `/api/auth/me` | Fetch authenticated user summary | Yes |

### 2. Category Management (`/api/categories`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/categories` | List all categories with expense counts | Yes |
| `GET` | `/api/categories/{id}` | Get single category by ID | Yes |
| `POST` | `/api/categories` | Create custom category | Yes |
| `PUT` | `/api/categories/{id}` | Update category name, icon, color | Yes |
| `DELETE` | `/api/categories/{id}` | Delete category (blocked if expenses linked) | Yes |

### 3. Expense Management (`/api/expenses`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/expenses` | Filter expenses (category, date range, keyword) | Yes |
| `GET` | `/api/expenses/{id}` | Get single expense | Yes |
| `POST` | `/api/expenses` | Record a new expense | Yes |
| `PUT` | `/api/expenses/{id}` | Update an existing expense | Yes |
| `DELETE` | `/api/expenses/{id}` | Delete an expense | Yes |

### 4. Budget Management (`/api/budgets`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/budgets/current` | Get budget for current month + usage stats | Yes |
| `GET` | `/api/budgets` | List all budgets or filter by `?year=&month=` | Yes |
| `POST` | `/api/budgets` | Set or update monthly budget | Yes |
| `PUT` | `/api/budgets/{id}` | Update budget by ID | Yes |
| `DELETE` | `/api/budgets/{id}` | Delete budget by ID | Yes |

### 5. Dashboard Summary (`/api/dashboard`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | Aggregated stats, trends, top expense, breakdown | Yes |

### 6. Reports & Export (`/api/reports`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/reports/summary` | Date-range financial summary & averages | Yes |
| `GET` | `/api/reports/export-csv` | Download RFC 4180 CSV spreadsheet | Yes |

---

## 🎨 Frontend Architecture & Features

The user interface uses modern CSS glassmorphism, responsive flex/grid layouts, micro-animations, and Chart.js integration:

1. **`login.html`**:
   - Modern glassmorphic card with tab switcher (Sign In / Create Account).
   - Form validation, password visibility toggles, loading spinners, alert notifications.
   - Redirects to `dashboard.html` if already logged in.

2. **`dashboard.html`**:
   - **Sidebar Navigation:** Dashboard, Expenses, Budgets, Categories, Reports, and Logout.
   - **Overview Section:** Current Month Expenses, Total Expenses, Active Budget Card with dynamic progress bar and alert pill, Highest Expense card, Monthly Spending Trend Chart, Category Breakdown Chart, and Recent Transactions table.
   - **Expenses Section:** Filter bar by Category, Start Date, End Date, and Search keyword; "+ Add Expense" modal; action buttons to edit and delete.
   - **Budgets Section:** Quick budget configuration modal, current vs historic budget overview, visual threshold indicators (Green = On track, Yellow = Approaching limit, Red = Exceeded).
   - **Categories Section:** Category grid showing icon, name, color pill, expense count, edit modal, and safe delete dialog.
   - **Reports Section:** Custom date-range picker, presets (This Month, Last 30 Days, Year to Date), summary metrics (Total Spent, Daily Average, Transactions Count), category spending distribution chart, and instant "Export to CSV" button.
   - **Modals & Dialogs:** Add/Edit Expense modal, Set Budget modal, Category modal, and Deletion confirmation modal.
   - **Toast Notifications:** Feedback on creation, updates, deletions, and error handling.
   - **Mobile Responsiveness:** Drawer navigation toggle with touch overlay for smaller screens.

---

## 🧪 Verification & Test Results

The automated test suite runs with:
```powershell
$env:DB_USER='root'; $env:DB_PASSWORD='Mahima@143'; .\mvnw.cmd test
```

### Test Suite Execution Summary:
- **`DatabaseConnectivityTest`** (3 tests)
  - `shouldConnectToDatabase` ✅ Passed
  - `shouldHaveAllRequiredTables` ✅ Passed (`users`, `categories`, `expenses`, `budgets`)
  - `shouldHaveCorrectColumnsInUsersTable` ✅ Passed
- **`ExpenseAndCategoryServiceTest`** (11 tests)
  - `testCategoriesCreatedOnRegistration` ✅ Passed
  - `testCreateAndPreventDuplicateCategory` ✅ Passed
  - `testCreateExpenseAndFilter` ✅ Passed
  - `testCrossUserIsolation` ✅ Passed (ensures users cannot view, edit, or delete other users' expenses or use other users' categories)
  - `testBudgetManagementAndCalculations` ✅ Passed (spent amount, remaining, % used, exceeded status)
  - `testDashboardSummaryAndAnalytics` ✅ Passed
  - `testReportSummaryAndCsvExport` ✅ Passed
  - `testUpdateAndDeleteExpense` ✅ Passed
  - `testUpdateAndDeleteCategory` ✅ Passed
  - `testDeleteCategoryWithExpensesFails` ✅ Passed (relational integrity check)
  - `testDeleteBudget` ✅ Passed
- **`TrackMyWalletApplicationTests`** (1 test)
  - `contextLoads` ✅ Passed

**Total:** **15 Tests Run, 0 Failures, 0 Errors, 0 Skipped (100% SUCCESS)**.

---

## 🚀 How to Run the Application

### 1. Database Credentials
Ensure MySQL 8.0 is running on `localhost:3306` with database `trackmywallet`. Set the credentials in PowerShell:
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "Mahima@143"
```
*(Note: `application.properties` is also configured with `Mahima@143` as the default fallback).*

### 2. Start the Server
Navigate to the project root directory and start Spring Boot:
```powershell
.\mvnw.cmd spring-boot:run
```

The application will start on port `8080`.

### 3. Access the Application
Open your web browser and navigate to:
- **Application URL:** [http://localhost:8080/login.html](http://localhost:8080/login.html)
- Visiting root [http://localhost:8080/](http://localhost:8080/) automatically redirects to the dashboard (or login page if not logged in).

---

## 🔒 Security & Data Isolation Guarantees

1. **Password Protection:** All passwords are encrypted with BCrypt (`spring-security-crypto`) before being saved to MySQL. Password hashes are never returned in DTOs.
2. **Session Authentication:** Protected API endpoints (`/api/**`) are guarded by `SessionInterceptor`. Unauthenticated calls receive HTTP `401 Unauthorized`.
3. **User Isolation:** All expense, category, budget, dashboard, and report queries extract `userId` directly from the server-managed HTTP session. Clients cannot pass arbitrary user IDs to access or modify another user's financial records.
4. **Relational Safety:** Categories linked to existing expenses cannot be deleted, preventing dangling foreign key references and data corruption.
