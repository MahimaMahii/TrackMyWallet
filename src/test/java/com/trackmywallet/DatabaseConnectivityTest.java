package com.trackmywallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DatabaseConnectivityTest - Stage 2 Verification Test.
 *
 * @SpringBootTest loads the FULL application context, including the real
 * MySQL DataSource configured in application.properties.
 *
 * This test verifies:
 *   1. The application can connect to MySQL
 *   2. All 4 required tables exist in the trackmywallet database
 */
@SpringBootTest
class DatabaseConnectivityTest {

    @Autowired
    private DataSource dataSource;

    /**
     * Test 1: Verify a live connection to MySQL can be established.
     */
    @Test
    void shouldConnectToDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(3)).isTrue();
            System.out.println("✅ DATABASE CONNECTION OK: " + conn.getMetaData().getURL());
            System.out.println("✅ Connected as user: " + conn.getMetaData().getUserName());
        }
    }

    /**
     * Test 2: Verify all 4 tables exist in the trackmywallet database.
     */
    @Test
    void shouldHaveAllRequiredTables() throws Exception {
        String[] requiredTables = {"users", "categories", "expenses", "budgets"};

        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {

            for (String table : requiredTables) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'trackmywallet' AND table_name = '" + table + "'"
                );
                rs.next();
                assertThat(rs.getInt(1))
                    .as("Table '%s' must exist in trackmywallet", table)
                    .isEqualTo(1);
                System.out.println("✅ Table verified: " + table);
                rs.close();
            }
        }
    }

    /**
     * Test 3: Verify columns in the users table match our JPA entity.
     */
    @Test
    void shouldHaveCorrectColumnsInUsersTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME FROM information_schema.columns " +
                "WHERE table_schema = 'trackmywallet' AND table_name = 'users' " +
                "ORDER BY ORDINAL_POSITION"
            );
            java.util.List<String> cols = new java.util.ArrayList<>();
            while (rs.next()) cols.add(rs.getString(1));

            assertThat(cols).contains("user_id", "username", "email", "password_hash", "full_name", "created_at");
            System.out.println("✅ users table columns verified: " + cols);
        }
    }
}
