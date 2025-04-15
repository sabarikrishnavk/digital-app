package com.sellaway.userservice.service;

import com.sellaway.userservice.dto.CreateUserRequest;
import com.sellaway.userservice.dto.UserResponse;
import com.sellaway.userservice.model.User;
import com.sellaway.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Add methods to check for existing user if not already in repository
    // (Spring Data JPA can generate these based on method names)
    // Add these methods to UserRepository interface:
    // boolean existsByUsername(String username);
    // boolean existsByUserId(String userId);


    @Transactional // Ensure atomicity
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            // Consider creating custom exceptions
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByUserId(request.getUserId())) {
             // Consider creating custom exceptions
            throw new IllegalArgumentException("User ID already exists: " + request.getUserId());
        }

        // 2. Create User entity
        User newUser = User.builder()
                .userId(request.getUserId())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password!
                // Set other fields if necessary (e.g., roles, enabled status)
                .build();

        // 3. Save user
        User savedUser = userRepository.save(newUser);

        // 4. Map to response DTO (excluding password)
        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .username(user.getUsername())
                // map other fields
                .build();
    }

    // Optional: Add methods for getUserById, updateUser, deleteUser etc.
}
