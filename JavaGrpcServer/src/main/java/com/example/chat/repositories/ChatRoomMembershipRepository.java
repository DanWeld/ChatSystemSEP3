package com.example.chat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.ChatRoom;
import com.example.chat.domain.ChatRoomMembership;
import com.example.chat.domain.User;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Integer> {
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
    Optional<ChatRoomMembership> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    List<ChatRoomMembership> findByChatRoom(ChatRoom chatRoom);
    List<ChatRoomMembership> findByUser(User user);
}

