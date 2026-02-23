package com.example;

import com.example.dto.UserDto;
import com.example.dto.UserProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Example controller for testing warmup functionality.
 */
@RestController
@RequestMapping("/api")
public class ExampleController {

    /**
     * Simple endpoint that tests basic connectivity.
     */
    @GetMapping("/test-connectivity")
    public ResponseEntity<Map<String, String>> testConnectivity() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "example-app");
        response.put("test", "basic-connectivity");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that tests application metadata retrieval.
     */
    @GetMapping("/test-metadata")
    public ResponseEntity<Map<String, Object>> testMetadata() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Example Application");
        response.put("version", "1.0.0");
        response.put("description", "Spring Boot application with Warmupdicator");
        response.put("test", "metadata-retrieval");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that tests response time performance.
     */
    @GetMapping("/test-response-time")
    public ResponseEntity<Map<String, String>> testResponseTime(@RequestParam(defaultValue = "1000") long delay) throws InterruptedException {
        Thread.sleep(delay);
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("delay", String.valueOf(delay));
        response.put("test", "response-time-performance");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that tests different HTTP status codes.
     */
    @GetMapping("/test-status-codes")
    public ResponseEntity<Map<String, String>> testStatusCodes(@RequestParam(defaultValue = "200") int code) {
        Map<String, String> response = new HashMap<>();
        response.put("requested_status", String.valueOf(code));
        response.put("test", "status-code-handling");
        return ResponseEntity.status(code).body(response);
    }

    /**
     * Endpoint that tests failure scenarios.
     */
    @GetMapping("/test-failure-scenarios")
    public ResponseEntity<Map<String, String>> testFailureScenarios(@RequestParam(defaultValue = "false") boolean fail) {
        if (fail) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Service unavailable");
            response.put("test", "failure-scenario");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "stable-endpoint");
        response.put("test", "failure-scenario");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that validates and echoes back the received request details.
     * Used to verify warmup is sending correct HTTP method, headers, and payload.
     */
    @GetMapping("/test-request-validation-get")
    public ResponseEntity<Map<String, Object>> testRequestValidationGet(HttpServletRequest request) {
        return createValidationResponse(request, null);
    }

    @PostMapping("/test-request-validation-post")
    public ResponseEntity<Map<String, Object>> testRequestValidationPost(
            HttpServletRequest request,
            @RequestBody(required = false) String body) {
        return createValidationResponse(request, body);
    }

    @PutMapping("/test-request-validation-put")
    public ResponseEntity<Map<String, Object>> testRequestValidationPut(
            HttpServletRequest request,
            @RequestBody(required = false) String body) {
        return createValidationResponse(request, body);
    }

    @DeleteMapping("/test-request-validation-delete")
    public ResponseEntity<Map<String, Object>> testRequestValidationDelete(HttpServletRequest request) {
        return createValidationResponse(request, null);
    }

    private ResponseEntity<Map<String, Object>> createValidationResponse(HttpServletRequest request, String body) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", request.getMethod());
        response.put("path", request.getRequestURI());
        response.put("query", request.getQueryString());
        
        // Echo back all headers
        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(name -> 
            headers.put(name, request.getHeader(name))
        );
        response.put("headers", headers);
        
        // Echo back body if present
        if (body != null && !body.trim().isEmpty()) {
            response.put("body", body);
            response.put("body_length", body.length());
        } else {
            response.put("body", null);
            response.put("body_length", 0);
        }
        
        response.put("test", "request-validation");
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint that tests request body handling.
     */
    @PostMapping("/test-post-request")
    public ResponseEntity<Map<String, Object>> testPostRequest(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("test", "post-request-body");
        response.put("received_body", requestBody);
        response.put("body_size", requestBody.size());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT endpoint that tests request update functionality.
     */
    @PutMapping("/test-put-request")
    public ResponseEntity<Map<String, Object>> testPutRequest(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("test", "put-request-body");
        response.put("updated_data", requestBody);
        response.put("operation", "updated");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE endpoint that tests deletion functionality.
     */
    @DeleteMapping("/test-delete-request")
    public ResponseEntity<Map<String, String>> testDeleteRequest(@RequestParam(defaultValue = "1") String id) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("test", "delete-request");
        response.put("deleted_id", id);
        response.put("operation", "deleted");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-dto")
    public ResponseEntity<UserDto> testDto(@RequestBody UserProfileDto dto) {
        UserDto userDto = new UserDto();
        userDto.setEmail("john.doe@example.com");
        userDto.setProfile(dto);

        return ResponseEntity.ok(userDto);
    }
}
