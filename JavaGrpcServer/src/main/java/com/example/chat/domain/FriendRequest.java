package com.example.chat.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity representing a friend request sent from one user to another.
 * Friend requests can be in one of three states: PENDING, ACCEPTED, or DECLINED.
 */
@Entity
@Table(name = "friend_requests")
public class FriendRequest {

    /**
     * Unique identifier for the friend request.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The user who sent the friend request.
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The user who received the friend request.
     */
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * The current status of the friend request.
     * Defaults to PENDING when created.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    /**
     * Timestamp when the friend request was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the friend request was responded to (accepted or declined).
     * Null if the request is still pending.
     */
    private Instant respondedAt;

    /**
     * Pre-persist lifecycle callback that sets the createdAt timestamp
     * to the current time if it hasn't been set yet.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Gets the unique identifier of the friend request.
     *
     * @return the friend request ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the friend request.
     *
     * @param id the friend request ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the user who sent the friend request.
     *
     * @return the sender user
     */
    public User getSender() {
        return sender;
    }

    /**
     * Sets the user who sent the friend request.
     *
     * @param sender the sender user to set
     */
    public void setSender(User sender) {
        this.sender = sender;
    }

    /**
     * Gets the user who received the friend request.
     *
     * @return the receiver user
     */
    public User getReceiver() {
        return receiver;
    }

    /**
     * Sets the user who received the friend request.
     *
     * @param receiver the receiver user to set
     */
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    /**
     * Gets the current status of the friend request.
     *
     * @return the status
     */
    public FriendRequestStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the friend request.
     *
     * @param status the status to set
     */
    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when the friend request was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the friend request was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the friend request was responded to.
     *
     * @return the response timestamp, or null if not yet responded
     */
    public Instant getRespondedAt() {
        return respondedAt;
    }

    /**
     * Sets the timestamp when the friend request was responded to.
     *
     * @param respondedAt the response timestamp to set
     */
    public void setRespondedAt(Instant respondedAt) {
        this.respondedAt = respondedAt;
    }
}

