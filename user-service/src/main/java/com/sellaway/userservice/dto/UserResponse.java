package com.sellaway.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents the user data returned to the client (no password)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id; // Internal DB id
    private String userId;
    private String username;
    // Add other non-sensitive fields (email, etc.) if they exist in User model
}
