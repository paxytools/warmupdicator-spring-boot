# Warmupdicator

**Warmupdicator Spring Boot Starter lets you define and run warm-up checks for HTTP endpoints, databases, or any custom resource, ensuring your app is ready for real traffic with minimal cold starts, and provides a dedicated Actuator health check for warm-up status.**

**Requirements:** Java 17+ and Spring Boot 3.0+

[![CI](https://github.com/paxytools/warmupdicator-spring-boot/actions/workflows/ci.yml/badge.svg)](https://github.com/paxytools/warmupdicator-spring-boot/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.paxytools/warmupdicator-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.paxytools/warmupdicator-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/release/paxytools/warmupdicator-spring-boot.svg)](https://github.com/paxytools/warmupdicator-spring-boot/releases)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/paxytools/warmupdicator-spring-boot/blob/main/LICENSE)

Warmupdicator Spring Boot provides warmup monitoring support for Spring Boot Applications.<br/>
There are 2 ways to integrate `warmupdicator-spring-boot` in your project:

- Simply adding the starter jar `warmupdicator-spring-boot-starter` to your classpath if using `@SpringBootApplication` or `@EnableAutoConfiguration` will enable warmup monitoring across the entire Spring Environment
- Adding `warmupdicator-spring-boot` to your classpath and adding `@EnableWarmupdicator` to your main Configuration class to enable warmup monitoring across the entire Spring Environment

## 🚀 Quick Start

### Method 1: Auto-Configuration (Recommended)

Simply add the starter jar dependency to your project if your Spring Boot application uses `@SpringBootApplication` or `@EnableAutoConfiguration` and warmup monitoring will be enabled across the entire Spring Environment (This means warmup checks will automatically run when the application starts):

```xml
<dependency>
    <groupId>io.github.paxytools</groupId>
    <artifactId>warmupdicator-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

That's it! No additional configuration needed. Warmup monitoring will be automatically enabled.

### Method 2: Manual Configuration

If you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations then add this dependency to your project:

```xml
<dependency>
    <groupId>io.github.paxytools</groupId>
    <artifactId>warmupdicator-spring-boot</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

And then add `@EnableWarmupdicator` to your Configuration class. For instance:

```java
@Configuration
@EnableWarmupdicator
public class MyApplication {
    ...
}
```

And warmup monitoring will be enabled across the entire Spring Environment.

## 🔧 How everything Works?

This will trigger some configuration to be loaded that basically does 4 things:

1. It registers a Spring post processor that detects all beans implementing the `Warmupdicator` interface
2. It creates `EndpointWarmupdicator` beans for each endpoint defined in configuration properties
3. It defines a `WarmupService` that executes all warmup indicators when the application starts
4. It registers a health indicator that reports the status of warmup checks

## ⚙️ Configuration

Add warmupdicator configuration to your `application.yaml`:

```yaml
warmupdicator:
  endpoint-warmer:
    endpoints:
      - url: http://localhost:8080/api/test
        max-response-time: 500
      - url: https://api.example.com/health
        http-method: GET
        max-response-time: 1000
        ignore-failure: false
```

**Note**: For better separation, you can also use `spring.config.imports` or `@PropertySource` to load configuration from dedicated files like `warmupdicator.yaml`.

| Property                                                      | Default        | Description                                  |
|---------------------------------------------------------------|----------------|----------------------------------------------|
| `warmupdicator.enabled`                                       | `true`         | Enable/disable warmup checks                 |
| `warmupdicator.show-details`                                  | `false`        | Show detailed information in health endpoint |
| **Endpoint Warmer**                                           |                |                                              |
| `warmupdicator.endpoint-warmer.enabled`                       | `true`         | Enable endpoint warmup                       |
| `warmupdicator.endpoint-warmer.endpoints`                     | `[]`           | List of HTTP endpoints to check              |
| `warmupdicator.endpoint-warmer.endpoints[].name`              | Auto-generated | Unique identifier for this endpoint          |
| `warmupdicator.endpoint-warmer.endpoints[].url`               | Required       | Full URL to call                             |
| `warmupdicator.endpoint-warmer.endpoints[].http-method`       | `GET`          | HTTP method to use                           |
| `warmupdicator.endpoint-warmer.endpoints[].max-response-time` | `500`          | Response time threshold in milliseconds      |
| `warmupdicator.endpoint-warmer.endpoints[].expected-status`   | Any 2xx        | Expected HTTP status                         |
| `warmupdicator.endpoint-warmer.endpoints[].ignore-failure`    | `false`        | Ignore failures and consider successful      |
| `warmupdicator.endpoint-warmer.endpoints[].request-body`      | `null`         | Request body for POST/PUT/PATCH requests     |
| `warmupdicator.endpoint-warmer.endpoints[].headers`           | `null`         | Custom HTTP headers as key-value pairs       |
| **DTO Warmer**                                                |                |                                              |
| `warmupdicator.dto-warmer.enabled`                            | `false`        | Enable DTO preloading warmup                 |
| `warmupdicator.dto-warmer.exclude-patterns`                   | `["*Record", "*Immutable"]` | DTO class patterns to exclude from warmup    |
| `warmupdicator.dto-warmer.warmup-serialization`               | `true`         | Warm up Jackson serialization                |
| `warmupdicator.dto-warmer.warmup-deserialization`             | `true`         | Warm up Jackson deserialization              |

## 🛠️ How do I create custom warmup indicators?

Create custom warmup indicators by implementing the `Warmupdicator` interface. They must be annotated with `@Component` to be detected by Spring's component scanning:

```java
@Component
public class DatabaseWarmupIndicator implements Warmupdicator {

    @Override
    public WarmupResult warmUp() {
        // Perform your warmup check here
        long startTime = System.currentTimeMillis();

        // Your warmup logic...
        try {
            // Check database connectivity
            boolean success = checkDatabaseConnection();
            
            long responseTimeMs = System.currentTimeMillis() - startTime;
            if (success) {
                return WarmupResult.success(responseTimeMs);
            } else {
                return WarmupResult.failure("Database connection failed", responseTimeMs);
            }
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            return WarmupResult.failure("Database warmup error: " + e.getMessage(), responseTimeMs);
        }
    }

    @Override
    public String getId() {
        return "database-connection";
    }
    
    private boolean checkDatabaseConnection() {
        // Your database connection logic here
        return true;
    }
}
```

**Important**: Custom warmup indicators must be annotated with `@Component` to be detected by Spring's component scanning.

## 🏥 Health Monitoring

Warmupdicator integrates seamlessly with Spring Boot Actuator by extending the built-in `HealthIndicator` system. This provides a standardized way to monitor warmup status alongside your application's other health checks.

Configure Spring Boot Actuator to expose health information:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always
```

Check warmup status:
```bash
curl http://localhost:8080/actuator/health
```

The health endpoint shows individual warmer results and overall status, making it easy to verify your application is fully warmed up and ready for traffic.

## ✨ Benefits

### Why Use Warmupdicator?

1. **Reduced First-Request Latency**: Pre-warms Jackson serializers, HTTP clients, and custom components
2. **Early Failure Detection**: Identifies connectivity and configuration issues during startup
3. **Health Monitoring**: Integrates with Spring Boot Actuator for comprehensive health checks
4. **Simple Configuration**: Minimal setup required - just add endpoints you want to warm up
5. **Flexible**: Supports both HTTP endpoints and custom warmup logic
6. **Production Ready**: Fail-safe design that never breaks application startup

### Performance Impact

- **Startup Time**: Adds minimal overhead (typically < 100ms for most applications)
- **Memory Usage**: Negligible memory footprint
- **First Request**: Can reduce first-request latency by 50-200ms depending on application complexity

## 📦 Modules

- **warmupdicator-spring-boot**: Contains core API and Spring integration (Warmupdicator interface, WarmupResult class, beans)
- **warmupdicator-spring-boot-starter**: Spring Boot integration with auto-configuration
- **warmupdicator-example**: Example application demonstrating usage

## 📚 Example Project

See `warmupdicator-example` module for a complete example of how to use Warmupdicator in a Spring Boot application.

## 📄 License

MIT License

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For more information, please see the [Contributing Guidelines](CONTRIBUTING.md).
