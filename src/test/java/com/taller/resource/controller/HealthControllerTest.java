package com.taller.resource.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void health_returnsUpWithoutDependencies() {
        assertThat(healthController.health()).isEqualTo(Map.of("status", "UP"));
    }
}
