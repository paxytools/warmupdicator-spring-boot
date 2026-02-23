package io.github.paxytools.warmupdicator.impl;

import io.github.paxytools.warmupdicator.api.WarmupResult;
import io.github.paxytools.warmupdicator.api.Warmupdicator;
import io.github.paxytools.warmupdicator.config.DtoWarmerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Warmup indicator that preloads DTO classes using Spring MVC HandlerMethod discovery.
 * Discovers request and response DTOs from controller methods and warms Jackson serialization/deserialization.
 * 
 * NOTE:
 * This implementation intentionally uses deprecated Jackson capability methods
 * (canSerialize / canDeserialize) because they are the only reliable way to force
 * eager serializer/deserializer construction without instantiating records.
 *
 * Newer Jackson APIs do not provide an equivalent trigger.
 */
@Slf4j
@RequiredArgsConstructor
public class DtoWarmupIndicator implements Warmupdicator {

    private final DtoWarmerProperties properties;
    private final ObjectMapper objectMapper;
    private final RequestMappingHandlerMapping handlerMapping;

    @Override
    public String getId() {
        return "dto-warmup";
    }

    @Override
    public WarmupResult warmUp() {
        log.info("Starting DTO warmup...");
        Instant start = Instant.now();
        
        try {
            // Discover DTO classes from HandlerMethods
            Set<Class<?>> dtoClasses = discoverDtoClasses();

            // Filter out excluded patterns
            Set<Class<?>> filteredClasses = filterExcludedClasses(dtoClasses);
            
            log.debug("DTO warmup candidates (after filter): {}", 
                filteredClasses.stream().map(Class::getName).toList());

            // Warm up each DTO class
            int warmedCount = 0;
            int skippedCount = 0;

            for (Class<?> dtoClass : filteredClasses) {
                boolean warmed = warmupDtoClass(dtoClass);
                if (warmed) {
                    warmedCount++;
                } else {
                    skippedCount++;
                }
            }

            long duration = java.time.Duration.between(start, Instant.now()).toMillis();

            log.info("DTO warmup completed successfully: {} warmed, {} skipped ({}ms)", warmedCount, skippedCount, duration);
            return WarmupResult.success(duration, 1);

        } catch (Exception e) {
            long duration = java.time.Duration.between(start, Instant.now()).toMillis();
            log.error("DTO warmup failed: {}", e.getMessage());
            return WarmupResult.failure("DTO warmup failed: " + e.getMessage(), duration, 1);
        }
    }

    /**
     * Discover DTO classes from all HandlerMethods in the application.
     */
    private Set<Class<?>> discoverDtoClasses() {
        Set<Class<?>> dtoClasses = new HashSet<>();

        for (HandlerMethod handlerMethod : handlerMapping.getHandlerMethods().values()) {
            // Extract request body classes
            extractRequestBodyClasses(handlerMethod, dtoClasses);

            // Extract response body classes
            extractResponseBodyClasses(handlerMethod, dtoClasses);
        }

        log.debug("Discovered {} DTO classes from {} handler methods", dtoClasses.size(), handlerMapping.getHandlerMethods().size());
        return dtoClasses;
    }

    /**
     * Extract request body classes from method parameters.
     */
    private void extractRequestBodyClasses(HandlerMethod handlerMethod, Set<Class<?>> dtoClasses) {
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

        for (MethodParameter methodParameter : methodParameters) {
            if (methodParameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)) {
                ResolvableType type = ResolvableType.forMethodParameter(methodParameter);
                Class<?> resolved = type.resolve();
                
                if (resolved != null && Collection.class.isAssignableFrom(resolved)) {
                    Class<?> generic = type.getGeneric(0).resolve();
                    if (generic != null) {
                        dtoClasses.add(generic);
                        log.debug("Found request body DTO (generic): {}", generic.getSimpleName());
                    }
                } else if (resolved != null && Optional.class.isAssignableFrom(resolved)) {
                    Class<?> generic = type.getGeneric(0).resolve();
                    if (generic != null) {
                        dtoClasses.add(generic);
                        log.debug("Found request body DTO (optional): {}", generic.getSimpleName());
                    }
                } else {
                    Class<?> parameterType = type.resolve();
                    if (parameterType != null) {
                        dtoClasses.add(parameterType);
                        log.debug("Found request body DTO: {}", parameterType.getSimpleName());
                    }
                }
            }
        }
    }

    /**
     * Extract response body classes from method return types.
     */
    private void extractResponseBodyClasses(
            HandlerMethod handlerMethod,
            Set<Class<?>> dtoClasses
    ) {
        ResolvableType resolvableType =
                ResolvableType.forMethodReturnType(handlerMethod.getMethod());

        ResolvableType bodyType = resolvableType;
        
        // Handle ResponseEntity<T>
        if (ResponseEntity.class.isAssignableFrom(resolvableType.toClass())) {
            bodyType = resolvableType.getGeneric(0);
        }
        
        // Handle Collection<T>
        Class<?> bodyResolved = bodyType.resolve();
        if (bodyResolved != null && Collection.class.isAssignableFrom(bodyResolved)) {
            Class<?> generic = bodyType.getGeneric(0).resolve();
            if (generic != null && generic != Void.TYPE && generic != String.class) {
                dtoClasses.add(generic);
                log.debug("Found response body DTO (collection): {}", generic.getSimpleName());
            }
            return;
        }
        
        // Handle Optional<T>
        if (bodyResolved != null && Optional.class.isAssignableFrom(bodyResolved)) {
            Class<?> generic = bodyType.getGeneric(0).resolve();
            if (generic != null && generic != Void.TYPE && generic != String.class) {
                dtoClasses.add(generic);
                log.debug("Found response body DTO (optional): {}", generic.getSimpleName());
            }
            return;
        }

        // Handle plain return types
        Class<?> resolved = bodyType.resolve();
        if (resolved != null
                && resolved != Void.TYPE
                && resolved != String.class) {

            dtoClasses.add(resolved);
            log.debug("Found response body DTO: {}", resolved.getSimpleName());
        }
    }

    /**
     * Filter out classes that match exclude patterns.
     */
    private Set<Class<?>> filterExcludedClasses(Set<Class<?>> dtoClasses) {
        Set<Class<?>> filtered = new HashSet<>();
        
        for (Class<?> dtoClass : dtoClasses) {
            // Skip framework types
            if (dtoClass.getPackageName().startsWith("org.springframework")) {
                log.debug("Skipping framework type: {}", dtoClass.getSimpleName());
                continue;
            }
            
            // Skip excluded patterns
            boolean excluded = false;
            for (String pattern : properties.getExcludePatterns()) {
                if (dtoClass.getSimpleName().matches(pattern.replace("*", ".*"))) {
                    log.debug("Excluded DTO from warmup: {}", dtoClass.getSimpleName());
                    excluded = true;
                    break;
                }
            }

            if (!excluded) {
                filtered.add(dtoClass);
            }
        }

        return filtered;
    }

    /**
     * Create an instance of a DTO class using no-args constructor.
     */
    private Object createDtoInstance(Class<?> dtoClass) throws Exception {
        if (dtoClass.isRecord()) {
            log.debug("Skipping record DTO: {}", dtoClass.getSimpleName());
            return null;
        }

        try {
            Constructor<?> constructor = dtoClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            log.debug("Cannot instantiate DTO {}: {}", dtoClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * Warm up a single DTO class.
     * @return true if the DTO was successfully warmed, false if skipped or failed
     */
    private boolean warmupDtoClass(Class<?> dtoClass) {
        log.debug("Warming up DTO class: {}", dtoClass.getSimpleName());
        
        try {
            // Skip obvious non-DTOs
            if (dtoClass.isEnum()
                || dtoClass.isPrimitive()
                || Number.class.isAssignableFrom(dtoClass)
                || CharSequence.class.isAssignableFrom(dtoClass)) {
                log.debug("Skipping non-DTO type: {}", dtoClass.getSimpleName());
                return false;
            }

            // Handle records specially - warm via Jackson capability checks
            if (dtoClass.isRecord()) {
                boolean warmed = false;
                if (properties.isWarmupSerialization()) {
                    try {
                        // Use deprecated but effective method to force serializer construction
                        @SuppressWarnings("deprecation")
                        boolean canSerialize = objectMapper.canSerialize(dtoClass);
                        if (canSerialize) {
                            warmed = true;
                            log.debug("Warmed up serialization for record: {}", dtoClass.getSimpleName());
                        }
                    } catch (Exception e) {
                        log.debug("Could not warm up serialization for record: {}", dtoClass.getSimpleName(), e);
                    }
                }
                if (properties.isWarmupDeserialization()) {
                    try {
                        // Use deprecated but effective method to force deserializer construction
                        @SuppressWarnings("deprecation")
                        boolean canDeserialize = objectMapper.canDeserialize(objectMapper.getTypeFactory().constructType(dtoClass));
                        if (canDeserialize) {
                            warmed = true;
                            log.debug("Warmed up deserialization for record: {}", dtoClass.getSimpleName());
                        }
                    } catch (Exception e) {
                        log.debug("Could not warm up deserialization for record: {}", dtoClass.getSimpleName(), e);
                    }
                }
                return warmed;
            }

            // Try to instantiate DTO for non-record classes
            Object instance = createDtoInstance(dtoClass);

            if (instance != null) {
                boolean warmed = false;
                
                // Warm up serialization
                byte[] serialized = null;
                if (properties.isWarmupSerialization()) {
                    serialized = objectMapper.writeValueAsBytes(instance);
                    warmed = true;
                    log.debug("Warmed up serialization for: {}", dtoClass.getSimpleName());
                }

                // Warm up deserialization (optional)
                if (properties.isWarmupDeserialization()) {
                    if (serialized != null) {
                        // Reuse serialized bytes if we already have them
                        objectMapper.readValue(serialized, dtoClass);
                        warmed = true;
                        log.debug("Warmed up deserialization for: {}", dtoClass.getSimpleName());
                    } else {
                        // Separate serialization if only deserialization is enabled
                        byte[] freshSerialized = objectMapper.writeValueAsBytes(instance);
                        objectMapper.readValue(freshSerialized, dtoClass);
                        warmed = true;
                        log.debug("Warmed up deserialization for: {}", dtoClass.getSimpleName());
                    }
                }
                
                return warmed;
            }
        } catch (Exception e) {
            log.debug("Failed to warm up DTO: {}", dtoClass.getSimpleName(), e);
        }
        
        return false;
    }
}
