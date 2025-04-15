package com.sellaway.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Optional: Add constructors if needed
import lombok.AllArgsConstructor; // Optional: Add constructors if needed
import lombok.Builder;       // Optional: Add builder if needed

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user") // Good practice to explicitly name the table
@Data
@Builder // Added builder for easier object creation (optional)
@NoArgsConstructor // Needed for JPA/Builder
@AllArgsConstructor // Needed for Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Internal database primary key

    @Column(unique = true, nullable = false) // Make userId unique and not null
    private String userId; // Public-facing user identifier (used as customerId)

    @Column(unique = true, nullable = false) // Make username unique and not null
    private String username; // Used for login

    @Column(nullable = false) // Password should not be null
    private String password; // Stores the hashed password

    // Add other fields as needed, e.g., email, roles, enabled status etc.
    // private String email;
    // private boolean enabled;
    // @Enumerated(EnumType.STRING)
    // private Role role; // Example if you have roles
}
