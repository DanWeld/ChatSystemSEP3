package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findByOwnerIdAndRoomType(Integer ownerId, com.example.chat.domain.ChatRoomType roomType);
}

