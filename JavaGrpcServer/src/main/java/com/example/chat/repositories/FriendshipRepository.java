package com.example.chat.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.Friendship;
import com.example.chat.domain.FriendshipId;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    List<Friendship> findByIdUserId(int userId);
    boolean existsByIdUserIdAndIdFriendId(int userId, int friendId);
}

