package com.example.chat.domain;

/**
 * Enumeration representing the status of a friend request.
 */
public enum FriendRequestStatus {
    /**
     * The friend request is waiting for a response from the receiver.
     */
    PENDING,

    /**
     * The friend request has been accepted by the receiver.
     */
    ACCEPTED,

    /**
     * The friend request has been declined by the receiver.
     */
    DECLINED
}

