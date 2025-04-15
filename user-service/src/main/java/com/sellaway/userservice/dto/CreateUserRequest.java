package com.sellaway.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "User ID cannot be blank")
    @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters")
    private String userId; // Public facing ID, ensure it's unique

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // Login username, ensure it's unique

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password; // Plain text password from client

    // Add other fields like email, firstName, lastName if needed
    // @Email
    // private String email;
}
