package com.sellaway.userservice.repository;

import com.sellaway.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // Add these methods for duplicate checks in UserService
    boolean existsByUsername(String username);
    boolean existsByUserId(String userId);

    // Optional<User> findByUserId(String userId); // If needed elsewhere
}
