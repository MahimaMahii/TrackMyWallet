package com.trackmywallet;

import com.trackmywallet.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class HealthControllerTest {

    @Autowired
    private HealthController healthController;

    @Test
    void testHealthEndpointReturnsUp() {
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("database")).isEqualTo("UP");
        assertThat(response.getBody()).containsKey("timestamp");
    }
}
