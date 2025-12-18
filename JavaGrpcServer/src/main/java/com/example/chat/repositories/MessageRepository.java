package com.example.chat.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.Message;

/**
 * Repository interface for managing Message entities.
 * Provides database operations for message-related data access.
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {
    /**
     * Finds all messages in a chat room, ordered by creation time.
     *
     * @param chatRoomId the ID of the chat room
     * @return a list of messages ordered by creation time (ascending)
     */
    List<Message> findByChatRoomIdOrderByCreatedAtAsc(Integer chatRoomId);

    /**
     * Finds messages in a chat room containing the specified text (case-insensitive).
     *
     * @param chatRoomId the ID of the chat room
     * @param text the text to search for
     * @return a list of matching messages ordered by creation time (ascending)
     */
    List<Message> findByChatRoomIdAndTextContainingIgnoreCaseOrderByCreatedAtAsc(Integer chatRoomId, String text);
}
