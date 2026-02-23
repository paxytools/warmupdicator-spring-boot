package io.github.paxytools.warmupdicator.api;

/**
 * Interface for warmup indicators.
 * Implementations of this interface can be registered as beans to be automatically
 * detected and used by the WarmupService.
 */
public interface Warmupdicator {

    /**
     * Executes the warmup check.
     *
     * @return the result of the warmup check
     */
    WarmupResult warmUp();

    /**
     * Returns a unique identifier for this warmup indicator.
     * This is used to track results and report status.
     *
     * @return a unique identifier
     */
    String getId();
}
