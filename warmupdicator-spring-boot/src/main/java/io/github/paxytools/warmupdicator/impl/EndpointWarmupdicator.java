package io.github.paxytools.warmupdicator.impl;

import io.github.paxytools.warmupdicator.api.Warmupdicator;
import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.config.EndpointWarmerProperties.EndpointProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Implementation of Warmupdicator that makes HTTP calls to an endpoint.
 */
@Slf4j
@RequiredArgsConstructor
public class EndpointWarmupdicator implements Warmupdicator {

    private final EndpointProperties endpoint;
    private final HttpClient httpClient;

    @Override
    public WarmupResult warmUp() {
        log.debug("Calling warmup endpoint: {} {}", endpoint.getHttpMethod(), endpoint.getUrl());

        Instant start = Instant.now();
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint.getUrl()))
                    .timeout(Duration.ofMillis(endpoint.getMaxResponseTime()));

            // Set HTTP method and body
            String method = endpoint.getHttpMethod() != null ? endpoint.getHttpMethod().toUpperCase() : "GET";
            HttpRequest.BodyPublisher bodyPublisher = endpoint.getRequestBody() != null 
                    ? HttpRequest.BodyPublishers.ofString(endpoint.getRequestBody())
                    : HttpRequest.BodyPublishers.noBody();
                
            // Set default Content-Type header, then apply custom headers (which can override defaults)
            requestBuilder.header("Content-Type", "application/json");
            if (endpoint.getHeaders() != null) {
                endpoint.getHeaders().forEach(requestBuilder::header);
            }
                
            HttpRequest request = requestBuilder
                    .method(method, bodyPublisher)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long responseTimeMs = Duration.between(start, Instant.now()).toMillis();

            // Check response status
            boolean statusOk = endpoint.getExpectedStatus() != null 
                    ? response.statusCode() == endpoint.getExpectedStatus()
                    : HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful();

            // Check response time
            boolean timeOk = responseTimeMs <= endpoint.getMaxResponseTime();

            if (statusOk && timeOk) {
                log.info("Warming up - {} {} succeeded", endpoint.getHttpMethod(), endpoint.getName());
                return WarmupResult.success(responseTimeMs, 1);
            } else {
                String errorMessage = !statusOk 
                        ? String.format("HTTP %d error for %s", response.statusCode(), endpoint.getName())
                        : String.format("Response time %dms exceeds acceptable threshold %dms for %s", responseTimeMs, endpoint.getMaxResponseTime(), endpoint.getUrl());
                
                if (endpoint.isIgnoreFailure()) {
                    log.warn("Ignoring failure for {} {}: {}", endpoint.getHttpMethod(), endpoint.getUrl(), errorMessage);
                    return WarmupResult.success(responseTimeMs, 1);
                }
                
                log.warn(errorMessage);
                return WarmupResult.failure(errorMessage, responseTimeMs);
            }
        } catch (Exception e) {
            long responseTimeMs = Duration.between(start, Instant.now()).toMillis();
            
            if (endpoint.isIgnoreFailure()) {
                log.warn("Ignoring exception for {} {}: {}", endpoint.getHttpMethod(), endpoint.getUrl(), e.getMessage());
                return WarmupResult.success(responseTimeMs, 1);
            }
            
            return WarmupResult.failure(e.getMessage(), responseTimeMs);
        }
    }

    @Override
    public String getId() {
        return endpoint.getName();
    }
}
