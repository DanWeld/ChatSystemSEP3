package com.example.chat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMembership;
import com.example.chat.domain.User;

/**
 * Repository interface for managing ChatRoomMembership entities.
 * Provides database operations for chat room membership-related data access.
 */
public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Integer> {
    /**
     * Checks if a membership exists for a user in a chat room.
     *
     * @param chatRoom the chat room to check
     * @param user the user to check
     * @return true if the membership exists, false otherwise
     */
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    /**
     * Finds the membership record for a user in a chat room.
     *
     * @param chatRoom the chat room
     * @param user the user
     * @return an Optional containing the membership if found, empty otherwise
     */
    Optional<ChatRoomMembership> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    /**
     * Finds all memberships for a specific chat room.
     *
     * @param chatRoom the chat room
     * @return a list of all memberships in the chat room
     */
    List<ChatRoomMembership> findByChatRoom(ChatRoom chatRoom);

    /**
     * Finds all memberships for a specific user.
     *
     * @param user the user
     * @return a list of all memberships the user belongs to
     */
    List<ChatRoomMembership> findByUser(User user);
}

