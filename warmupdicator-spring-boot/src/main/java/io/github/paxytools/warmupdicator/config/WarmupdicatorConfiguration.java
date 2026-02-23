package io.github.paxytools.warmupdicator.config;

import io.github.paxytools.warmupdicator.api.Warmupdicator;
import io.github.paxytools.warmupdicator.health.WarmupHealthIndicator;
import io.github.paxytools.warmupdicator.impl.DtoWarmupIndicator;
import io.github.paxytools.warmupdicator.impl.EndpointWarmupdicator;
import io.github.paxytools.warmupdicator.service.WarmupService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import java.net.http.HttpClient;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual configuration for Warmupdicator when not using Spring Boot auto-configuration.
 * Enabled by {@link io.github.paxytools.warmupdicator.annotation.EnableWarmupdicator}.
 */
@Configuration
@ConditionalOnProperty(prefix = "warmupdicator", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(WarmupdicatorConfiguration.class)
public class WarmupdicatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "warmupdicator.endpoint-warmer", name = "enabled", havingValue = "true", matchIfMissing = true)
    public List<EndpointWarmupdicator> endpointWarmupdicators(WarmupdicatorProperties properties, HttpClient httpClient) {
        return properties.getEndpointWarmer().getEndpoints().stream()
                .map(endpoint -> new EndpointWarmupdicator(endpoint, httpClient))
                .collect(Collectors.toList());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "warmupdicator.dto-warmer", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DtoWarmupIndicator dtoWarmupIndicator(WarmupdicatorProperties properties, ObjectMapper objectMapper, @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        return new DtoWarmupIndicator(properties.getDtoWarmer(), objectMapper, handlerMapping);
    }

    @Bean
    public WarmupService warmupService(
            ObjectProvider<List<Warmupdicator>> componentsWarmupdicatorProvider,
            ObjectProvider<List<EndpointWarmupdicator>> endpointWarmupdicatorProvider,
            ObjectProvider<DtoWarmupIndicator> dtoWarmupIndicatorProvider
    ) {
        Set<Warmupdicator> allWarmers = new HashSet<>();

        componentsWarmupdicatorProvider.ifAvailable(allWarmers::addAll);
        endpointWarmupdicatorProvider.ifAvailable(allWarmers::addAll);
        dtoWarmupIndicatorProvider.ifAvailable(allWarmers::add);

        return new WarmupService(allWarmers);
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("warmup")
    @ConditionalOnMissingBean(name = "warmupHealthIndicator")
    public HealthIndicator warmupHealthIndicator(WarmupService warmupService, WarmupdicatorProperties properties) {
        return new WarmupHealthIndicator(warmupService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WarmupdicatorProperties warmupdicatorProperties() {
        return new WarmupdicatorProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
