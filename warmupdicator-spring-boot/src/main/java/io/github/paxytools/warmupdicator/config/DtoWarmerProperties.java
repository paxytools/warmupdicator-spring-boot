package io.github.paxytools.warmupdicator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for DTO preloading warmup.
 */
@Data
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "warmupdicator.dto-warmer")
public class DtoWarmerProperties {
    
    /**
     * Enable DTO preloading warmup.
     */
    private boolean enabled = false;
    
    /**
     * DTO class patterns to exclude from warmup.
     * Supports wildcards like "*Record" or "*Immutable".
     */
    private List<String> excludePatterns = new ArrayList<>(List.of("*Record", "*Immutable"));
    
    /**
     * Warm up Jackson serialization by calling objectMapper.writeValueAsBytes(dtoInstance).
     */
    private boolean warmupSerialization = true;
    
    /**
     * Warm up Jackson deserialization.
     * 
     * Feasibility is determined by:
     * 1. For records: Uses Jackson's canDeserialize() capability check
     * 2. For non-records: Requires successful DTO instantiation and serialization first
     * 
     * Default: true (failsafe handling prevents startup failures)
     */
    private boolean warmupDeserialization = true;
}
