package com.example.chat.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatRoomIdOrderByCreatedAtAsc(Integer chatRoomId);

    List<Message> findByChatRoomIdAndTextContainingIgnoreCaseOrderByCreatedAtAsc(Integer chatRoomId, String text);
}
