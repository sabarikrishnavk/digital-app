package com.sellaway.userservice.service;

import com.sellaway.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User; // Use Spring's User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections; // For empty authorities list

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Inject your repository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.sellaway.userservice.model.User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Adapt your User entity to Spring Security's UserDetails
        // For simplicity here, we assume username is unique and use basic User.
        // You might create a custom UserDetails implementation holding the customerId.
        // Or implement UserDetails directly in your User entity.
        return new org.springframework.security.core.userdetails.User( // Fully qualify to avoid ambiguity if UserDetails is implemented in model.User
                appUser.getUsername(),
                appUser.getPassword(),
                Collections.emptyList() // Add authorities/roles here if you have them
        );
    }

    // Helper method to get customerId after authentication (if not stored in UserDetails)
    public String getCustomerIdByUsername(String username) {
         com.sellaway.userservice.model.User appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
         // CORRECTED: Use getUserId() which returns the String identifier
         return appUser.getUserId();
    }
}
