package io.github.paxytools.warmupdicator.annotation;

import io.github.paxytools.warmupdicator.config.WarmupdicatorConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables Warmupdicator beans for applications not using
 * Spring Boot auto-configuration. Use this annotation on a configuration
 * class to manually enable warmup checks.
 *
 * Example:
 * <pre>
 * {@code
 * @Configuration
 * @EnableWarmupdicator
 * public class MyApplication {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WarmupdicatorConfiguration.class)
public @interface EnableWarmupdicator {
}
