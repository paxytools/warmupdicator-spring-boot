package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("roles")
    private List<String> roles;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("is_active")
    private boolean isActive;
    
    @JsonProperty("profile")
    private UserProfileDto profile;
    
    // Static initializer that simulates expensive initialization
    static {
        // Simulate some expensive class loading/initialization
        try {
            Thread.sleep(50); // Simulate 50ms of initialization work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("UserDto class initialized");
    }
    
    public static UserDto createSample() {
        return new UserDto(
            1L,
            "john.doe",
            "john.doe@example.com",
            "John Doe",
            List.of("USER", "ADMIN"),
            LocalDateTime.now(),
            true,
            new UserProfileDto("Developer", "Engineering")
        );
    }
}
