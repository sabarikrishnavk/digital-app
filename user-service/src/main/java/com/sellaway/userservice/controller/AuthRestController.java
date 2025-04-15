package com.sellaway.userservice.controller;

import com.sellaway.userservice.dto.AuthenticationRequest;
import com.sellaway.userservice.dto.AuthenticationResponse;
import com.sellaway.userservice.dto.CreateUserRequest; // Import new DTO
import com.sellaway.userservice.dto.UserResponse; // Import new DTO
import com.sellaway.userservice.service.AppUserDetailsService;
import com.sellaway.userservice.service.JwtService;
import com.sellaway.userservice.service.UserService; // Import UserService
import jakarta.validation.Valid; // Import validation annotation
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // Keep user creation under /auth for simplicity, or create a new /users controller
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService; // Renamed for clarity
    private final JwtService jwtService;
    private final UserService userService; // Inject UserService

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request // Add @Valid
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getUsername());
        final String customerId = appUserDetailsService.getCustomerIdByUsername(request.getUsername());
        final String jwt = jwtService.generateToken(userDetails, customerId);

        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwt).build());
    }

    // --- New User Creation Endpoint ---
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody CreateUserRequest request // Use @Valid for input validation
    ) {
        UserResponse createdUser = userService.createUser(request);
        // Return 201 Created status with the created user details (without password)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    // --- End New User Creation Endpoint ---
}
