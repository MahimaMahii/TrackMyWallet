package com.trackmywallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic Spring Boot context load test.
 * Verifies the application starts without errors.
 */
@SpringBootTest
class TrackMyWalletApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context starts successfully, this test passes.
        // It verifies that all beans (entities, repositories, etc.) are wired correctly.
        System.out.println("✅ Spring Application Context loaded successfully.");
    }
}
