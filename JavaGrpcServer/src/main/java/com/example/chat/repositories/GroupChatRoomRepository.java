package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.GroupChatRoom;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Integer> {
    Optional<GroupChatRoom> findByChatRoomId(Integer chatRoomId);
}

