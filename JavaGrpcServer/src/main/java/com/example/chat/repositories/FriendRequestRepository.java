package com.example.chat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.domain.FriendRequest;
import com.example.chat.domain.FriendRequestStatus;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    Optional<FriendRequest> findBySenderIdAndReceiverId(int senderId, int receiverId);
    List<FriendRequest> findByReceiverIdAndStatus(int receiverId, FriendRequestStatus status);
}

