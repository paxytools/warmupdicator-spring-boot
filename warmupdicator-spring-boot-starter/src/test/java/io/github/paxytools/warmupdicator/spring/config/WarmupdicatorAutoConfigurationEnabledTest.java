package io.github.paxytools.warmupdicator.spring.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@SpringBootTest(classes = WarmupdicatorAutoConfiguration.class)
@TestPropertySource(properties = "warmupdicator.enabled=true")
class WarmupdicatorAutoConfigurationEnabledTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testAutoConfigurationEnabledWhenPropertyTrue() {
        // Test passes - context should load when warmupdicator is enabled
        assertNotNull(applicationContext, "Application context should be loaded");
        
        // Verify that warmup-related beans are created
        assertTrue(applicationContext.containsBeanDefinition("warmupService"), "WarmupService bean should be created");
        
        // Verify that the context contains expected beans
        assertTrue(applicationContext.containsBeanDefinition("warmupService"), "Context should contain warmupService bean");
    }
}
