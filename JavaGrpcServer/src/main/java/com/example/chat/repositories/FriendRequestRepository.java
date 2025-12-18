package com.example.chat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.FriendRequest;
import com.example.chat.domain.FriendRequestStatus;

/**
 * Repository interface for managing FriendRequest entities.
 * Provides database operations for friend request-related data access.
 */
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    /**
     * Finds a friend request by sender and receiver IDs.
     *
     * @param senderId the ID of the sender
     * @param receiverId the ID of the receiver
     * @return an Optional containing the friend request if found, empty otherwise
     */
    Optional<FriendRequest> findBySenderIdAndReceiverId(int senderId, int receiverId);

    /**
     * Finds all friend requests for a receiver with a specific status.
     *
     * @param receiverId the ID of the receiver
     * @param status the status of the requests to find
     * @return a list of friend requests matching the criteria
     */
    List<FriendRequest> findByReceiverIdAndStatus(int receiverId, FriendRequestStatus status);
}

