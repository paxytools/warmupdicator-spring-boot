package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    @JsonProperty("job_title")
    private String jobTitle;
    
    @JsonProperty("department")
    private String department;
    
    // Static initializer that simulates expensive initialization
    static {
        try {
            Thread.sleep(30); // Simulate 30ms of initialization work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("UserProfileDto class initialized");
    }
}
