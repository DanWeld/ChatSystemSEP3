package com.example.chat.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.PrivateChatRoom;
import com.example.chat.domain.User;

public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Integer> {
    Optional<PrivateChatRoom> findByUserAAndUserB(User userA, User userB);
    Optional<PrivateChatRoom> findByUserAIdAndUserBId(int userAId, int userBId);
}

