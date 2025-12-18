package com.example.chat.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.Friendship;
import com.example.chat.domain.FriendshipId;

/**
 * Repository interface for managing Friendship entities.
 * Provides database operations for friendship-related data access.
 */
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    /**
     * Finds all friendships for a given user.
     *
     * @param userId the ID of the user
     * @return a list of friendships where the user is the owner
     */
    List<Friendship> findByIdUserId(int userId);

    /**
     * Checks if a friendship exists between two users.
     *
     * @param userId the ID of the user
     * @param friendId the ID of the friend
     * @return true if the friendship exists, false otherwise
     */
    boolean existsByIdUserIdAndIdFriendId(int userId, int friendId);
}

