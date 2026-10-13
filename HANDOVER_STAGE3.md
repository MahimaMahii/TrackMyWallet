# 🚀 TrackMyWallet — Project Status & Teammate Handover

**Date:** October 12, 2026  
**Current Milestone Completed:** **Stage 3 (User Authentication & Session Management)**  
**Overall Project Health:** 🟢 All systems operational, test suite passing (100%), server running live.

---

## 📌 Quick Summary for Your Teammate

| Attribute | Details |
|---|---|
| **Project Name** | TrackMyWallet |
| **Workspace Location** | `c:\Users\AMRUTHA HARIKUMAR\OneDrive\Desktop\TrackMyWallet` |
| **Backend Tech Stack** | Java 21, Spring Boot 4.1.1, Spring Data JPA / Hibernate 7, BCrypt (`spring-security-crypto`) |
| **Frontend Tech Stack** | HTML5, CSS3 (Glassmorphism dark theme), JavaScript (Fetch API) |
| **Database** | MySQL 8.0 (`localhost:3306`, database: `trackmywallet`) |
| **Build & Tooling** | Maven Wrapper (`.\mvnw.cmd`) |
| **Active Port** | `http://localhost:8080` |

---

## ✅ What Has Been Implemented & Verified

### Stage 1: Project Setup & Architecture
- Configured Spring Boot 4.1.1 with Java 21.
- Set up Maven Wrapper (`mvnw.cmd`) and `pom.xml` dependencies.

### Stage 2: Database Design & JPA Entities
- Created MySQL database `trackmywallet`.
- Created SQL schema script in [`sql/trackmywallet_schema.sql`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/sql/trackmywallet_schema.sql).
- Implemented 4 JPA Entity models:
  - [`User.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/model/User.java) (`users` table)
  - [`Category.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/model/Category.java) (`categories` table)
  - [`Expense.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/model/Expense.java) (`expenses` table)
  - [`Budget.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/model/Budget.java) (`budgets` table)
- Implemented 4 Spring Data JPA repositories:
  - [`UserRepository.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/repository/UserRepository.java)
  - [`CategoryRepository.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/repository/CategoryRepository.java)
  - [`ExpenseRepository.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/repository/ExpenseRepository.java)
  - [`BudgetRepository.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/repository/BudgetRepository.java)
- Configured HikariCP pool and schema validation in [`application.properties`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/resources/application.properties).

### Stage 3: Authentication & Security (Completed & Tested)
- **Data Transfer Objects (DTOs):**
  - [`RegisterRequest.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/dto/RegisterRequest.java) — Registration validation (`@NotBlank`, `@Email`, `@Size`)
  - [`LoginRequest.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/dto/LoginRequest.java) — Login payload
  - [`ApiResponse.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/dto/ApiResponse.java) — Unified JSON response structure
  - [`UserSummary.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/dto/UserSummary.java) — Safe user representation (never exposes password hash)
- **Business Logic & Service:**
  - [`UserService.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/service/UserService.java):
    - Registers new users with BCrypt password hashing.
    - Automatically provisions 6 default categories upon registration (Food & Dining, Rent & Housing, Shopping, Entertainment, Bills & Utilities, Salary & Income).
    - Verifies credentials securely on login.
- **REST Controller & Security:**
  - [`AuthController.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/controller/AuthController.java):
    - `POST /api/auth/register` — Create user account
    - `POST /api/auth/login` — Authenticate and create HTTP session
    - `POST /api/auth/logout` — Invalidate session
    - `GET /api/auth/me` — Return current session user
  - [`SessionInterceptor.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/config/SessionInterceptor.java) — Intercepts unauthenticated calls to protected routes (`/api/*`) and returns `401 Unauthorized`.
  - [`WebConfig.java`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/java/com/trackmywallet/config/WebConfig.java) — Registers the session interceptor and static asset routing.
- **Frontend UI:**
  - [`login.html`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/resources/static/login.html) — Tabbed sign-in / sign-up UI with form validation and loading feedback.
  - [`css/auth.css`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/resources/static/css/auth.css) — Custom glassmorphism dark theme styling with micro-animations.
  - [`js/auth.js`](file:///c:/Users/AMRUTHA%20HARIKUMAR/OneDrive/Desktop/TrackMyWallet/src/main/resources/static/js/auth.js) — Client-side interaction and Fetch API calls to `/api/auth/*`.

---

## 🧪 Verification & Test Results

### 1. Automated Test Suite (4/4 Passed)
Run with:
```powershell
$env:DB_USER='root'; $env:DB_PASSWORD='amrutha123@'; .\mvnw.cmd test
```
Result:
```text
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
- `DatabaseConnectivityTest.shouldConnectToDatabase` ✅ Passed
- `DatabaseConnectivityTest.shouldHaveAllRequiredTables` ✅ Passed (users, categories, expenses, budgets)
- `DatabaseConnectivityTest.shouldHaveCorrectColumnsInUsersTable` ✅ Passed
- `TrackMyWalletApplicationTests.contextLoads` ✅ Passed

### 2. Live HTTP & Session Endpoints
- `GET http://localhost:8080/login.html` ➡️ `200 OK`
- `POST /api/auth/register` ➡️ `200 OK` (User registered + default categories initialized)
- `POST /api/auth/login` ➡️ `200 OK` (Session created)
- `GET /api/auth/me` (Authenticated) ➡️ `200 OK` (Returns user data)
- `POST /api/auth/logout` ➡️ `200 OK` (Session destroyed)
- `GET /api/auth/me` (Unauthenticated) ➡️ `401 Unauthorized` (SessionInterceptor guard working)

---

## 💻 How to Run the Project (For Your Teammate)

### Environment Variables
Before running the app or tests, set the MySQL credentials in PowerShell:
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "amrutha123@"
```

### Start Application Server
```powershell
cd "c:\Users\AMRUTHA HARIKUMAR\OneDrive\Desktop\TrackMyWallet"
$env:DB_USER='root'; $env:DB_PASSWORD='amrutha123@'
.\mvnw.cmd spring-boot:run
```
Once started, access:
- **Login / Register Page:** [http://localhost:8080/login.html](http://localhost:8080/login.html)

---

## 📋 What Needs to Be Done Next (Stage 4)

Your next teammate can immediately start **Stage 4: Expense & Category Management**:
1. **Expense Service & Category Service**:
   - Add/edit/delete expense operations with category relationships.
   - List user expenses with pagination / date filtering.
   - Create custom categories (or edit colors/icons).
2. **REST Endpoints**:
   - `GET /api/expenses` & `POST /api/expenses`
   - `PUT /api/expenses/{id}` & `DELETE /api/expenses/{id}`
   - `GET /api/categories` & `POST /api/categories`
3. **Frontend Dashboard UI**:
   - Build `dashboard.html`, `dashboard.css`, and `dashboard.js`.
   - Add expense modal, summary cards (Total Spent, Monthly Budget, Remaining Balance), and recent transactions list.
