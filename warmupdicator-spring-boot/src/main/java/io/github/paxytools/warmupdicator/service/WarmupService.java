package io.github.paxytools.warmupdicator.service;

import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.api.Warmupdicator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for performing warmup checks.
 */
@Slf4j
@RequiredArgsConstructor
public class WarmupService {
    private final Set<Warmupdicator> warmers;

    @Getter
    private boolean warmedUp = false;

    @Getter
    private Map<String, WarmupResult> results = new HashMap<>();

    @Getter
    private long totalTimeMs = 0;

    @Getter
    private int totalTries = 0;

    @Getter
    private int attemptNumber = 0;

    /**
     * Performs warmup checks when the application is ready.
     * Executes all configured warmers in parallel with retry logic for failed attempts.
     * This method is automatically triggered by the ApplicationReadyEvent.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void performWarmup() {
        if (warmers.isEmpty()) {
            log.info("No warmup warmers configured, skipping warmup");
            warmedUp = true;
            return;
        }

        log.info("Starting warmup for {} warmers", warmers.size());
        Instant start = Instant.now();

        while (true) {

            // If this is a retry, log it
            if (attemptNumber > 0) {
                log.info("Retry attempt {} for failed warmers", attemptNumber);
            }

            // Try all warmers that haven't succeeded yet in parallel
            Map<String, CompletableFuture<WarmupResult>> pendingFutures = new HashMap<>();
            for (Warmupdicator warmer : warmers) {
                String id = warmer.getId();
                WarmupResult existingResult = results.get(id);

                // Skip warmers that have already succeeded
                if (existingResult != null && existingResult.isSuccess()) {
                    continue;
                }

                // Execute the warmer
                log.debug("{} warmer: {}", attemptNumber == 0 ? "Executing" : "Retrying", id);
                pendingFutures.put(id, CompletableFuture.supplyAsync(warmer::warmUp));
            }

            if (!pendingFutures.isEmpty()) {
                CompletableFuture.allOf(pendingFutures.values().toArray(new CompletableFuture[0])).join();

                for (Map.Entry<String, CompletableFuture<WarmupResult>> entry : pendingFutures.entrySet()) {
                    String id = entry.getKey();
                    WarmupResult result = entry.getValue().join();
                    
                    // Update attempt count to reflect service-level retry
                    WarmupResult updatedResult = result.isSuccess() 
                        ? WarmupResult.success(result.getResponseTimeMs(), attemptNumber + 1)
                        : WarmupResult.failure(result.getMessage(), result.getResponseTimeMs(), attemptNumber + 1);
                    
                    results.put(id, updatedResult);
                    totalTries++;

                    if (updatedResult.isSuccess()) {
                        log.info("Warmup succeeded - {} (attempt {})", id, updatedResult.getAttemptCount());
                    } else {
                        log.info("Warmup failed - {} (attempt {}): {}", id, updatedResult.getAttemptCount(), updatedResult.getMessage());
                    }
                }
            }

            // Check if all warmers have been processed and succeeded
            if (results.size() == warmers.size() &&
                results.values().stream().allMatch(WarmupResult::isSuccess)) {
                break;
            } else {
                // Log which warmers are still failing
                Set<String> failingWarmers = warmers.stream()
                    .map(Warmupdicator::getId)
                    .filter(id -> {
                        WarmupResult result = results.get(id);
                        return result == null || !result.isSuccess();
                    })
                    .collect(Collectors.toSet());
                
                if (!failingWarmers.isEmpty()) {
                    log.info("Warmup retry needed for failing warmers: [{}]",
                            String.join(",\n", failingWarmers));
                }
            }

            attemptNumber++;
            totalTimeMs = Duration.between(start, Instant.now()).toMillis();
        }

        if (results.values().stream().allMatch(WarmupResult::isSuccess)) {
            int totalAttempts = results.values().stream().mapToInt(WarmupResult::getAttemptCount).sum();
            log.info("Warmup completed successfully in {}ms after {} tries ({} total attempts)", totalTimeMs, totalTries, totalAttempts);
            warmedUp = true;
        }
    }
}
