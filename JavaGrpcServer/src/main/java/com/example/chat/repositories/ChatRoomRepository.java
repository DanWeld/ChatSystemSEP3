package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.ChatRoom;

/**
 * Repository interface for managing ChatRoom entities.
 * Provides database operations for chat room-related data access.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    /**
     * Finds a chat room by owner ID and room type.
     *
     * @param ownerId the ID of the room owner
     * @param roomType the type of the chat room
     * @return an Optional containing the chat room if found, empty otherwise
     */
    Optional<ChatRoom> findByOwnerIdAndRoomType(Integer ownerId, com.example.chat.domain.ChatRoomType roomType);
}

