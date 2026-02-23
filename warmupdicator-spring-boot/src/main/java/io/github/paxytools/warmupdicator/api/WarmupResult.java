package io.github.paxytools.warmupdicator.api;

import lombok.Getter;

/**
 * Represents the result of a warmup check.
 */
@Getter
public class WarmupResult {
    private final boolean success;
    private final String message;
    private final long responseTimeMs;
    private final int attemptCount;

    private WarmupResult(boolean success, String message, long responseTimeMs, int attemptCount) {
        this.success = success;
        this.message = message;
        this.responseTimeMs = responseTimeMs;
        this.attemptCount = attemptCount;
    }

    /**
     * Creates a successful warmup result.
     *
     * @param responseTimeMs the response time in milliseconds
     * @return a successful WarmupResult
     */
    public static WarmupResult success(long responseTimeMs) {
        return success(responseTimeMs, 1);
    }

    /**
     * Creates a successful warmup result with attempt count.
     *
     * @param responseTimeMs the response time in milliseconds
     * @param attemptCount the number of attempts made
     * @return a successful WarmupResult
     */
    public static WarmupResult success(long responseTimeMs, int attemptCount) {
        return new WarmupResult(true, "OK", responseTimeMs, attemptCount);
    }

    /**
     * Creates a failed warmup result.
     *
     * @param message the failure message
     * @param responseTimeMs the response time in milliseconds
     * @return a failed WarmupResult
     */
    public static WarmupResult failure(String message, long responseTimeMs) {
        return failure(message, responseTimeMs, 1);
    }

    /**
     * Creates a failed warmup result with attempt count.
     *
     * @param message the failure message
     * @param responseTimeMs the response time in milliseconds
     * @param attemptCount the number of attempts made
     * @return a failed WarmupResult
     */
    public static WarmupResult failure(String message, long responseTimeMs, int attemptCount) {
        return new WarmupResult(false, message, responseTimeMs, attemptCount);
    }
}
