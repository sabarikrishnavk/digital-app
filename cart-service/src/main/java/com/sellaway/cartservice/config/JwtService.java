package com.sellaway.cartservice.config; // Or security package

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);


    @Value("${jwt.secret}")
    private String secretKey;

    public String extractUsername(String token) {
         try {
             return extractClaim(token, Claims::getSubject);
         } catch (Exception e) {
             log.error("Error extracting username from token: {}", e.getMessage());
             return null;
         }
    }

    public String extractCustomerId(String token) {
         try {
             Claims claims = extractAllClaims(token);
             return claims.get("customerId", String.class); // Extract custom claim
         } catch (Exception e) {
             log.error("Error extracting customerId from token: {}", e.getMessage());
             return null;
         }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Simplified validation for the filter - just checks expiration and structure
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
             log.warn("Invalid JWT token: {}", e.getMessage());
             return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
         // It's important this parsing logic and key retrieval matches the user-service
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
