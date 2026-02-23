package com.example;

import io.github.paxytools.warmupdicator.api.Warmupdicator;
import io.github.paxytools.warmupdicator.api.WarmupResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Example of a custom Warmupdicator implementation that checks database connectivity.
 */
@Slf4j
@Component
public class ExampleWarmupdicator_2 implements Warmupdicator {

    private final DataSource dataSource;

    public ExampleWarmupdicator_2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public WarmupResult warmUp() {
        log.info("Checking database connectivity...");
        long startTime = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            long responseTimeMs = System.currentTimeMillis() - startTime;

            if (valid) {
                log.info("Database connection successful in {}ms", responseTimeMs);
                return WarmupResult.success(responseTimeMs);
            } else {
                log.warn("Database connection check failed");
                return WarmupResult.failure("Database connection is not valid", responseTimeMs);
            }
        } catch (SQLException e) {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            log.error("Database connection error: {}", e.getMessage());
            return WarmupResult.failure("Database connection error: " + e.getMessage(), responseTimeMs);
        }
    }

    @Override
    public String getId() {
        return "database-connection";
    }
}
