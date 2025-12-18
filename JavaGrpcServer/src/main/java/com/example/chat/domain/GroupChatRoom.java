package com.example.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a group chat room with additional metadata.
 * This class extends the basic chat room concept with group-specific properties
 * such as name, description, and privacy settings.
 */
@Entity
@Table(name = "group_chat_rooms")
public class GroupChatRoom {

    /**
     * The chat room ID that this group chat extends.
     * This is also the primary key of this entity.
     */
    @Id
    @Column(name = "chat_room_id")
    private Integer chatRoomId;

    /**
     * The name of the group chat room.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Optional description of the group chat room.
     */
    private String description;

    /**
     * Flag indicating whether the group chat is private.
     * Private groups may require invitation to join.
     */
    @Column(name = "is_private", nullable = false)
    private boolean privateRoom = false;

    /**
     * Gets the chat room ID.
     *
     * @return the chat room ID
     */
    public Integer getChatRoomId() {
        return chatRoomId;
    }

    /**
     * Sets the chat room ID.
     *
     * @param chatRoomId the chat room ID to set
     */
    public void setChatRoomId(Integer chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    /**
     * Gets the name of the group chat.
     *
     * @return the group chat name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group chat.
     *
     * @param name the group chat name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the group chat.
     *
     * @return the description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the group chat.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks if the group chat is private.
     *
     * @return true if the group chat is private, false otherwise
     */
    public boolean isPrivateRoom() {
        return privateRoom;
    }

    /**
     * Sets whether the group chat is private.
     *
     * @param privateRoom true to make the group chat private, false otherwise
     */
    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }
}

