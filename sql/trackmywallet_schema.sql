-- ============================================================
--  TrackMyWallet Database Schema
--  MySQL 8.0
--  Run this script once to set up the database.
--  Usage: mysql -u root -p < sql/trackmywallet_schema.sql
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS trackmywallet
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE trackmywallet;

-- ============================================================
--  TABLE 1: users
--  Stores one row per registered person.
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT            AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)    NOT NULL UNIQUE,
    email         VARCHAR(100)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)   NOT NULL,           -- BCrypt hash, NEVER plain text
    full_name     VARCHAR(100)   NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email    (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  TABLE 2: categories
--  Each user owns their own categories (no sharing).
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    category_id INT          AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    icon        VARCHAR(50)  DEFAULT 'tag',           -- optional emoji / icon name
    color       VARCHAR(10)  DEFAULT '#6C63FF',       -- optional hex color for UI
    UNIQUE KEY uq_user_category (user_id, name),
    CONSTRAINT fk_cat_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_categories_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  TABLE 3: expenses
--  Each expense belongs to a user and a category.
-- ============================================================
CREATE TABLE IF NOT EXISTS expenses (
    expense_id   INT            AUTO_INCREMENT PRIMARY KEY,
    user_id      INT            NOT NULL,
    category_id  INT            NOT NULL,
    amount       DECIMAL(10,2)  NOT NULL CHECK (amount > 0),
    description  VARCHAR(200)   DEFAULT NULL,
    expense_date DATE           NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_exp_cat  FOREIGN KEY (category_id)
        REFERENCES categories (category_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_expenses_user         (user_id),
    INDEX idx_expenses_category     (category_id),
    INDEX idx_expenses_date         (expense_date),
    INDEX idx_expenses_user_date    (user_id, expense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  TABLE 4: budgets
--  One monthly budget per user per (year, month).
-- ============================================================
CREATE TABLE IF NOT EXISTS budgets (
    budget_id    INT            AUTO_INCREMENT PRIMARY KEY,
    user_id      INT            NOT NULL,
    budget_year  INT            NOT NULL,
    budget_month INT            NOT NULL CHECK (budget_month BETWEEN 1 AND 12),
    amount       DECIMAL(10,2)  NOT NULL CHECK (amount > 0),
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_budget (user_id, budget_year, budget_month),
    CONSTRAINT fk_bud_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_budgets_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  DONE: Schema created successfully.
-- ============================================================
SELECT 'trackmywallet schema created successfully!' AS status;
