package com.sellaway.userservice.controller;

import com.sellaway.userservice.dto.AuthenticationResponse;
import com.sellaway.userservice.dto.CreateUserRequest; // Import new DTO
import com.sellaway.userservice.dto.UserResponse; // Import new DTO
import com.sellaway.userservice.service.AppUserDetailsService;
import com.sellaway.userservice.service.JwtService;
import com.sellaway.userservice.service.UserService; // Import UserService
import jakarta.validation.Valid; // Import validation annotation
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated; // For validating @Argument

@Controller
@RequiredArgsConstructor
@Validated // Enable validation for method arguments annotated with @Valid
public class AuthGraphQLController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService; // Renamed for clarity
    private final JwtService jwtService;
    private final UserService userService; // Inject UserService

    @MutationMapping
    public AuthenticationResponse login(@Argument String username, @Argument String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        final UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);
        final String customerId = appUserDetailsService.getCustomerIdByUsername(username);
        final String jwt = jwtService.generateToken(userDetails, customerId);

        return AuthenticationResponse.builder().token(jwt).build();
    }

    // --- New User Creation Mutation ---
    @MutationMapping
    public UserResponse createUser(
            // Use a single input type argument for better schema structure and validation
            @Argument @Valid CreateUserRequest input
    ) {
        // The @Valid annotation combined with @Validated on the class
        // will trigger validation for the input object.
        return userService.createUser(input);
    }
    // --- End New User Creation Mutation ---
}

