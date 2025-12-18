package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.GroupChatRoom;

/**
 * Repository interface for managing GroupChatRoom entities.
 * Provides database operations for group chat room-related data access.
 */
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Integer> {
    /**
     * Finds a group chat room by its associated chat room ID.
     *
     * @param chatRoomId the ID of the associated chat room
     * @return an Optional containing the group chat room if found, empty otherwise
     */
    Optional<GroupChatRoom> findByChatRoomId(Integer chatRoomId);
}

