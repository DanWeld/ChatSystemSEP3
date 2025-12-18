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
 * Entity representing a chat room in the chat system.
 * This class defines the properties and relationships of a chat room,
 * including its type, owner, and creation timestamp.
 */
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    /**
     * Unique identifier for the chat room.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Type of the chat room (e.g., PRIVATE, GROUP).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private ChatRoomType roomType;

    /**
     * Owner of the chat room. May be null for some room types.
     */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * Timestamp when the chat room was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

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
     * Gets the unique identifier of the chat room.
     *
     * @return the chat room ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the chat room.
     *
     * @param id the chat room ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the type of the chat room.
     *
     * @return the room type
     */
    public ChatRoomType getRoomType() {
        return roomType;
    }

    /**
     * Sets the type of the chat room.
     *
     * @param roomType the room type to set
     */
    public void setRoomType(ChatRoomType roomType) {
        this.roomType = roomType;
    }

    /**
     * Gets the owner of the chat room.
     *
     * @return the owner user, or null if no owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the chat room.
     *
     * @param owner the owner user to set
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Gets the timestamp when the chat room was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the chat room was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

