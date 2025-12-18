package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.PrivateChatRoom;
import com.example.chat.domain.User;

/**
 * Repository interface for managing PrivateChatRoom entities.
 * Provides database operations for private chat room-related data access.
 */
public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Integer> {
    /**
     * Finds a private chat room by the two participating users.
     *
     * @param userA the first user
     * @param userB the second user
     * @return an Optional containing the private chat room if found, empty otherwise
     */
    Optional<PrivateChatRoom> findByUserAAndUserB(User userA, User userB);

    /**
     * Finds a private chat room by the IDs of the two participating users.
     *
     * @param userAId the ID of the first user
     * @param userBId the ID of the second user
     * @return an Optional containing the private chat room if found, empty otherwise
     */
    Optional<PrivateChatRoom> findByUserAIdAndUserBId(int userAId, int userBId);
}

