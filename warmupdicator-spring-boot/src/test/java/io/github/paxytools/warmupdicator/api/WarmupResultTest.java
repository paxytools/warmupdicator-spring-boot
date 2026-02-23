package io.github.paxytools.warmupdicator.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WarmupResultTest {

    @Test
    void testSuccess() {
        WarmupResult result = WarmupResult.success(100);
        assertTrue(result.isSuccess());
        assertEquals(100, result.getResponseTimeMs());
        assertEquals(1, result.getAttemptCount());
        assertEquals("OK", result.getMessage());
    }

    @Test
    void testSuccessWithAttempts() {
        WarmupResult result = WarmupResult.success(200, 3);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getResponseTimeMs());
        assertEquals(3, result.getAttemptCount());
        assertEquals("OK", result.getMessage());
    }

    @Test
    void testFailure() {
        WarmupResult result = WarmupResult.failure("Test failure", 150);
        assertFalse(result.isSuccess());
        assertEquals(150, result.getResponseTimeMs());
        assertEquals(1, result.getAttemptCount());
        assertEquals("Test failure", result.getMessage());
    }

    @Test
    void testFailureWithAttempts() {
        WarmupResult result = WarmupResult.failure("Another failure", 300, 5);
        assertFalse(result.isSuccess());
        assertEquals(300, result.getResponseTimeMs());
        assertEquals(5, result.getAttemptCount());
        assertEquals("Another failure", result.getMessage());
    }
}
