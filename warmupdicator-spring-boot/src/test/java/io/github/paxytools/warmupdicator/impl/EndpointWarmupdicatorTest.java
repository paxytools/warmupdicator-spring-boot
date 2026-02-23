package io.github.paxytools.warmupdicator.impl;

import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.config.EndpointWarmerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EndpointWarmupdicatorTest {

    @Mock
    private HttpClient httpClient;

    private EndpointWarmerProperties.EndpointProperties endpoint;
    private EndpointWarmupdicator endpointWarmupdicator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        endpoint = new EndpointWarmerProperties.EndpointProperties();
        endpoint.setUrl("http://example.com");
        endpoint.setMaxResponseTime(1000);
        endpoint.setIgnoreFailure(false);
        endpointWarmupdicator = new EndpointWarmupdicator(endpoint, httpClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSuccessfulWarmup() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any())).thenAnswer(invocation -> response);

        WarmupResult result = endpointWarmupdicator.warmUp();

        assertTrue(result.isSuccess());
        assertTrue(result.getResponseTimeMs() >= 0);
        assertEquals(1, result.getAttemptCount());
    }

    @Test
    void testTimeoutFailure() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Timeout"));

        WarmupResult result = endpointWarmupdicator.warmUp();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Timeout"));
        assertEquals(1, result.getAttemptCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHttpError() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), any())).thenAnswer(invocation -> response);

        WarmupResult result = endpointWarmupdicator.warmUp();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("HTTP 500 error"));
        assertEquals(1, result.getAttemptCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSlowResponseWithIgnore() throws Exception {
        endpoint.setIgnoreFailure(true);
        endpoint.setMaxResponseTime(10); // Very low threshold
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any())).thenAnswer(invocation -> response);

        WarmupResult result = endpointWarmupdicator.warmUp();

        assertTrue(result.isSuccess()); // Should succeed despite slowness
    }

    @Test
    void testGetId() {
        String id = endpointWarmupdicator.getId();
        assertNotNull(id);
        assertTrue(id.contains("-")); // Should contain UUID separator
        assertTrue(id.matches(".*-[a-f0-9]{8}")); // Should end with UUID suffix
    }
}
