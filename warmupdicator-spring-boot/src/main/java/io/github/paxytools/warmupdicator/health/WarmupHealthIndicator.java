package io.github.paxytools.warmupdicator.health;

import io.github.paxytools.warmupdicator.config.WarmupdicatorProperties;
import io.github.paxytools.warmupdicator.service.WarmupService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator that reports the status of warmup checks.
 */
@RequiredArgsConstructor
public class WarmupHealthIndicator implements HealthIndicator {

    private final WarmupService warmupService;
    private final WarmupdicatorProperties properties;

    @Override
    public Health health() {
        boolean isWarmedUp = warmupService.isWarmedUp();
        Health.Builder builder = isWarmedUp ? Health.up() : Health.down();

        // Add simplified status information
        String status = isWarmedUp ? "OK" : "FAIL";
        builder.withDetail("status", status);
        builder.withDetail("timeMs", warmupService.getTotalTimeMs());
        builder.withDetail("tries", warmupService.getTotalTries());

        // Add detailed information if enabled
        if (properties.isShowDetails()) {
            Map<String, Object> details = new HashMap<>();

            warmupService.getResults().forEach((id, result) -> {
                String resultMessage = result.isSuccess() 
                    ? String.format("OK (in %dms, attempts: %d)", result.getResponseTimeMs(), result.getAttemptCount())
                    : String.format("%s (took %dms, attempts: %d)", result.getMessage(), result.getResponseTimeMs(), result.getAttemptCount());
                details.put(id, resultMessage);
            });

            builder.withDetail("details", details);
        }

        return builder.build();
    }
}
