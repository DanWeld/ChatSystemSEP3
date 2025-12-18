package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.User;

/**
 * Repository interface for managing User entities.
 * Provides database operations for user-related data access.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
}

