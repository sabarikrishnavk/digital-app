package com.sellaway.cartservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // For empty authorities

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    // We don't need UserDetailsService here if we trust the JWT claims directly
    // after validation. We extract info directly from the token.

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String customerId; // We primarily care about the customerId now

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue without authentication
            return;
        }

        jwt = authHeader.substring(7); // Extract token after "Bearer "

        if (!jwtService.isTokenValid(jwt)) {
             logger.warn("Invalid or expired JWT token received.");
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Or handle appropriately
             // Optionally write an error response body
             // response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
             // response.setContentType("application/json");
             return; // Stop filter chain for invalid token
        }

        customerId = jwtService.extractCustomerId(jwt); // Extract customerId from validated token

        // If token is valid and we have customerId, set authentication in context
        if (customerId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // We don't have full UserDetails, but we can create a principal
            // representing the authenticated user based on the token claims.
            // Using customerId as the principal name here.
            // We grant a dummy authority or none if roles aren't in the token.
             UserDetails userDetails = User.builder()
                     .username(customerId) // Use customerId as the principal identifier
                     .password("") // Password not needed/available
                     .authorities(Collections.emptyList()) // Add authorities if included in JWT claims
                     .build();


            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, // Principal (can be UserDetails or just the customerId String)
                    null,       // Credentials (not needed for JWT)
                    userDetails.getAuthorities() // Authorities (can be empty)
            );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            // Update SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
         // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
