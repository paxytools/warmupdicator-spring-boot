package io.github.paxytools.warmupdicator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Configuration for HTTP endpoint warmup targets.
 */
@Data
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "warmupdicator.endpoint-warmer")
public class EndpointWarmerProperties {
    
    /**
     * Enable/disable this specific endpoint warmup.
     */
    private boolean enabled = true;
    
    /**
     * List of HTTP endpoint warmup targets.
     */
    private List<EndpointProperties> endpoints = new ArrayList<>();
    
    /**
     * Configuration for a single warmup endpoint.
     */ 
    @Data
    public static class EndpointProperties {
        /**
         * Unique identifier for this endpoint. If not provided, will be generated automatically.
         */
        private String name;

        /**
         * HTTP method to use (GET, POST, PUT, DELETE, HEAD, PATCH, OPTIONS).
         * Default: GET
         */
        private String httpMethod = "GET";

        /**
         * Full URL to call.
         */
        private String url;

        /**
         * Custom HTTP headers.
         */
        private java.util.Map<String, String> headers;

        /**
         * Request body/payload for POST/PUT/PATCH requests.
         */
        private String requestBody;

        /**
         * Maximum acceptable response time threshold in milliseconds.
         * Also used as HTTP request timeout.
         */
        private long maxResponseTime = 500;

        /**
         * Expected HTTP status code (accepts any 2xx if not set).
         */
        private Integer expectedStatus;

        /**
         * If true, failures will be ignored and endpoint will be considered successful.
         */
        private boolean ignoreFailure = false;

        /**
         * Gets unique name for this endpoint. If name is not set, uses URL with unique suffix.
         */
        public String getName() {
            if (name == null || name.trim().isEmpty()) {
                String baseUrl = url != null ? url : "unknown";
                // Extract last part of URL and clean it
                String urlPart = baseUrl.substring(baseUrl.lastIndexOf('/') + 1);
                urlPart = urlPart.replaceAll("[^a-zA-Z0-9]", "-");
                urlPart = urlPart.replaceAll("-+", "-").replaceAll("^-|-$", "");
                if (urlPart.isEmpty()) {
                    urlPart = "endpoint";
                }
                name = urlPart + "-" + UUID.randomUUID().toString().substring(0, 8);
            }
            return name;
        }
    }
}
