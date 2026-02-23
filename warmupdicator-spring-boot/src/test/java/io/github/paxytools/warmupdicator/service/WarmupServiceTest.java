package io.github.paxytools.warmupdicator.service;

import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.api.Warmupdicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WarmupServiceTest {

    @Mock
    private Warmupdicator warmer1;

    @Mock
    private Warmupdicator warmer2;

    private WarmupService warmupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        warmupService = new WarmupService(Set.of(warmer1, warmer2));
    }

    @Test
    void testPerformWarmupAllSuccess() {
        when(warmer1.getId()).thenReturn("warmer1");
        when(warmer2.getId()).thenReturn("warmer2");
        when(warmer1.warmUp()).thenReturn(WarmupResult.success(100));
        when(warmer2.warmUp()).thenReturn(WarmupResult.success(200));

        warmupService.performWarmup();

        assertTrue(warmupService.isWarmedUp());
        assertEquals(2, warmupService.getResults().size());
        assertTrue(warmupService.getResults().get("warmer1").isSuccess());
        assertTrue(warmupService.getResults().get("warmer2").isSuccess());
    }

    @Test
    void testPerformWarmupWithFailure() {
        when(warmer1.getId()).thenReturn("warmer1");
        when(warmer2.getId()).thenReturn("warmer2");
        when(warmer1.warmUp()).thenReturn(WarmupResult.success(100));
        // Simulate warmer2 failing 2 times, then succeeding on 3rd attempt
        when(warmer2.warmUp())
            .thenReturn(WarmupResult.failure("Failed", 200))
            .thenReturn(WarmupResult.failure("Failed", 200))
            .thenReturn(WarmupResult.success(200));

        warmupService.performWarmup();

        assertTrue(warmupService.isWarmedUp()); // Should succeed after retries
        assertEquals(2, warmupService.getResults().size());
        assertTrue(warmupService.getResults().get("warmer1").isSuccess());
        assertTrue(warmupService.getResults().get("warmer2").isSuccess());
        assertEquals(2, warmupService.getAttemptNumber()); // Should be 2 (0,1,2)
    }

    @Test
    void testNoWarmers() {
        warmupService = new WarmupService(Set.of());
        warmupService.performWarmup();

        assertTrue(warmupService.isWarmedUp());
        assertEquals(0, warmupService.getResults().size());
    }
}
