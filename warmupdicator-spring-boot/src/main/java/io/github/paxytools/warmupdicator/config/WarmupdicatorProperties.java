package io.github.paxytools.warmupdicator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Warmupdicator library.
 */
@Data
@ConfigurationProperties(prefix = "warmupdicator")
public class WarmupdicatorProperties {

    /**
     * Enable/disable warmup checks (default: true)
     * Can be overridden with system property: -Dwarmupdicator.enabled=true/false
     */
    private boolean enabled = true;

    /**
     * Show detailed information in health endpoint.
     */
    private boolean showDetails = false;

    /**
     * DTO preloading warmup configuration.
     */
    private DtoWarmerProperties dtoWarmer = new DtoWarmerProperties();

    /**
     * HTTP endpoint warmup configuration.
     */
    private EndpointWarmerProperties endpointWarmer = new EndpointWarmerProperties();
    
}
