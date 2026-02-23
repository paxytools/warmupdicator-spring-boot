package io.github.paxytools.warmupdicator.health;

import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.config.WarmupdicatorProperties;
import io.github.paxytools.warmupdicator.service.WarmupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarmupHealthIndicatorTest {

    private WarmupService warmupService;
    private WarmupdicatorProperties properties;
    private WarmupHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        warmupService = mock(WarmupService.class);
        properties = new WarmupdicatorProperties();
        healthIndicator = new WarmupHealthIndicator(warmupService, properties);
    }

    @Test
    void testHealthWhenWarmedUp() {
        when(warmupService.isWarmedUp()).thenReturn(true);
        when(warmupService.getTotalTimeMs()).thenReturn(100L);
        when(warmupService.getTotalTries()).thenReturn(2);
        when(warmupService.getResults()).thenReturn(new HashMap<>());

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("OK", health.getDetails().get("status"));
        assertEquals(100L, health.getDetails().get("timeMs"));
        assertEquals(2, health.getDetails().get("tries"));
    }

    @Test
    void testHealthWhenNotWarmedUp() {
        when(warmupService.isWarmedUp()).thenReturn(false);
        when(warmupService.getTotalTimeMs()).thenReturn(0L);
        when(warmupService.getTotalTries()).thenReturn(1);
        when(warmupService.getResults()).thenReturn(new HashMap<>());

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("FAIL", health.getDetails().get("status"));
        assertEquals(0L, health.getDetails().get("timeMs"));
        assertEquals(1, health.getDetails().get("tries"));
    }

    @Test
    void testHealthWithDetailsEnabled() {
        properties.setShowDetails(true);
        
        Map<String, WarmupResult> results = new HashMap<>();
        results.put("endpoint1", WarmupResult.success(50));
        results.put("endpoint2", WarmupResult.failure("Timeout", 1000));
        
        when(warmupService.isWarmedUp()).thenReturn(false);
        when(warmupService.getTotalTimeMs()).thenReturn(1050L);
        when(warmupService.getTotalTries()).thenReturn(3);
        when(warmupService.getResults()).thenReturn(results);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("details"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.getDetails().get("details");
        assertEquals("OK (in 50ms, attempts: 1)", details.get("endpoint1"));
        assertEquals("Timeout (took 1000ms, attempts: 1)", details.get("endpoint2"));
    }

    @Test
    void testHealthWithDetailsDisabled() {
        properties.setShowDetails(false);
        
        Map<String, WarmupResult> results = new HashMap<>();
        results.put("endpoint1", WarmupResult.success(50));
        
        when(warmupService.isWarmedUp()).thenReturn(true);
        when(warmupService.getTotalTimeMs()).thenReturn(50L);
        when(warmupService.getTotalTries()).thenReturn(1);
        when(warmupService.getResults()).thenReturn(results);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertFalse(health.getDetails().containsKey("details"));
        assertEquals("OK", health.getDetails().get("status"));
        assertEquals(50L, health.getDetails().get("timeMs"));
        assertEquals(1, health.getDetails().get("tries"));
    }

    @Test
    void testHealthWithMixedResults() {
        properties.setShowDetails(true);
        
        Map<String, WarmupResult> results = new HashMap<>();
        results.put("success", WarmupResult.success(100, 2));
        results.put("failure", WarmupResult.failure("Error", 200, 3));
        
        when(warmupService.isWarmedUp()).thenReturn(false);
        when(warmupService.getTotalTimeMs()).thenReturn(300L);
        when(warmupService.getTotalTries()).thenReturn(5);
        when(warmupService.getResults()).thenReturn(results);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) health.getDetails().get("details");
        assertEquals("OK (in 100ms, attempts: 2)", details.get("success"));
        assertEquals("Error (took 200ms, attempts: 3)", details.get("failure"));
    }
}
