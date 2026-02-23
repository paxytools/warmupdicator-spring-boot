package com.example;

import io.github.paxytools.warmupdicator.api.Warmupdicator;
import io.github.paxytools.warmupdicator.api.WarmupResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Example implementation of Warmupdicator that always succeeds after a short delay.
 * This is provided as a reference for users who want to create their own custom warmup indicators.
 */
@Slf4j
@Component
public class ExampleWarmupdicator_1 implements Warmupdicator {

    @Override
    public WarmupResult warmUp() {
        log.debug("Executing example warmup check: {}", getId());
        long startTime = System.currentTimeMillis();

        try {
            // Simulate some warmup work
            Thread.sleep(500);

            // This is where you would put your actual warmup logic
            // For example, checking if a cache is initialized, a connection is established, etc.

            long responseTimeMs = System.currentTimeMillis() - startTime;
            return WarmupResult.success(responseTimeMs);
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            return WarmupResult.failure("Example warmup check failed: " + e.getMessage(), responseTimeMs);
        }
    }

    @Override
    public String getId() {
        return "methodtest";
    }
}
