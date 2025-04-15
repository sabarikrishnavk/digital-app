 package com.sellaway.userservice.config;
 
 import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sellaway.userservice.service.AppUserDetailsService;

import lombok.RequiredArgsConstructor;
 
 @Configuration
 @EnableWebSecurity
 @RequiredArgsConstructor
 public class SecurityConfig {
 
     private final AppUserDetailsService userDetailsService;
 
     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         http
             .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
             .authorizeHttpRequests(auth -> auth
                // "/auth/**" already covers "/auth/register" and "/auth/login"
                .requestMatchers("/auth/**", "/graphql", "/graphiql/**").permitAll()
                .anyRequest().authenticated()
            )
             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Stateless session
             // We don't need the JWT filter here *yet* as we are only *generating* tokens
 
         return http.build();
     }
 
     @Bean
     public AuthenticationProvider authenticationProvider() {
         DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
         authProvider.setUserDetailsService(userDetailsService);
         authProvider.setPasswordEncoder(passwordEncoder());
         return authProvider;
     }
 
     @Bean
     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
         return config.getAuthenticationManager();
     }
 
     @Bean
     public PasswordEncoder passwordEncoder() {
         return new BCryptPasswordEncoder();
     }
 }
 