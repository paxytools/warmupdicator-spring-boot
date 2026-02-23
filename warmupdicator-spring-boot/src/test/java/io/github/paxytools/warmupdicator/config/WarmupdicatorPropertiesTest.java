package io.github.paxytools.warmupdicator.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarmupdicatorPropertiesTest {

    private WarmupdicatorProperties properties;

    @BeforeEach
    void setUp() {
        properties = new WarmupdicatorProperties();
    }

    @Test
    void testDefaultValues() {
        assertTrue(properties.isEnabled());
        assertFalse(properties.isShowDetails());
        assertNotNull(properties.getEndpointWarmer().getEndpoints());
        assertTrue(properties.getEndpointWarmer().getEndpoints().isEmpty());
    }

    @Test
    void testSetEnabled() {
        properties.setEnabled(false);
        assertFalse(properties.isEnabled());
        
        properties.setEnabled(true);
        assertTrue(properties.isEnabled());
    }

    @Test
    void testSetShowDetails() {
        properties.setShowDetails(true);
        assertTrue(properties.isShowDetails());
        
        properties.setShowDetails(false);
        assertFalse(properties.isShowDetails());
    }

    @Test
    void testEndpointsList() {
        assertNotNull(properties.getEndpointWarmer().getEndpoints());
        
        EndpointWarmerProperties.EndpointProperties endpoint = new EndpointWarmerProperties.EndpointProperties();
        endpoint.setUrl("http://example.com");
        
        properties.getEndpointWarmer().getEndpoints().add(endpoint);
        assertEquals(1, properties.getEndpointWarmer().getEndpoints().size());
        assertEquals("http://example.com", properties.getEndpointWarmer().getEndpoints().get(0).getUrl());
    }

    @Test
    void testWarmupEndpointDefaults() {
        EndpointWarmerProperties.EndpointProperties endpoint = new EndpointWarmerProperties.EndpointProperties();
        
        assertNull(endpoint.getUrl());
        assertEquals(500, endpoint.getMaxResponseTime());
        assertNull(endpoint.getExpectedStatus());
        assertFalse(endpoint.isIgnoreFailure());
    }

    @Test
    void testWarmupEndpointSettersAndGetters() {
        EndpointWarmerProperties.EndpointProperties endpoint = new EndpointWarmerProperties.EndpointProperties();
        
        endpoint.setUrl("https://test.com");
        endpoint.setMaxResponseTime(1000);
        endpoint.setExpectedStatus(200);
        endpoint.setIgnoreFailure(true);
        
        assertEquals("https://test.com", endpoint.getUrl());
        assertEquals(1000, endpoint.getMaxResponseTime());
        assertEquals(200, endpoint.getExpectedStatus());
        assertTrue(endpoint.isIgnoreFailure());
    }

    @Test
    void testWarmupEndpointToString() {
        EndpointWarmerProperties.EndpointProperties endpoint = new EndpointWarmerProperties.EndpointProperties();
        endpoint.setUrl("http://test.com");
        
        String toString = endpoint.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("url=http://test.com"));
    }

    @Test
    void testPropertiesToString() {
        properties.setEnabled(false);
        properties.setShowDetails(true);
        
        String toString = properties.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("enabled=false"));
        assertTrue(toString.contains("showDetails=true"));
    }

    @Test
    void testMultipleEndpoints() {
        EndpointWarmerProperties.EndpointProperties endpoint1 = new EndpointWarmerProperties.EndpointProperties();
        endpoint1.setUrl("http://first.com");
        
        EndpointWarmerProperties.EndpointProperties endpoint2 = new EndpointWarmerProperties.EndpointProperties();
        endpoint2.setUrl("http://second.com");
        
        properties.getEndpointWarmer().getEndpoints().add(endpoint1);
        properties.getEndpointWarmer().getEndpoints().add(endpoint2);
        
        assertEquals(2, properties.getEndpointWarmer().getEndpoints().size());
        assertEquals("http://first.com", properties.getEndpointWarmer().getEndpoints().get(0).getUrl());
        assertEquals("http://second.com", properties.getEndpointWarmer().getEndpoints().get(1).getUrl());
    }
}
