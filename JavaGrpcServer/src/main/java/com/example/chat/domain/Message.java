package com.example.chat.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity representing a message in a chat room.
 * This class stores message content, metadata, and edit/delete information.
 */
@Entity
@Table(name = "messages")
public class Message {

    /**
     * Unique identifier for the message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The ID of the chat room this message belongs to.
     */
    @Column(name = "chat_room_id", nullable = false)
    private Integer chatRoomId;

    /**
     * The ID of the user who sent this message.
     */
    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    /**
     * The text content of the message. Maximum length is 2000 characters.
     */
    @Column(length = 2000, nullable = false)
    private String text;

    /**
     * Timestamp when the message was created.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the message was last edited, if applicable.
     */
    @Column(name = "edited_at")
    private Instant editedAt;

    /**
     * Timestamp when the message was deleted, if applicable.
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * The ID of the user who deleted this message, if applicable.
     */
    @Column(name = "deleted_by")
    private Integer deletedBy;

    /**
     * Flag indicating whether this message has been edited.
     */
    @Column(name = "is_edited", nullable = false)
    private boolean edited = false;

    /**
     * Flag indicating whether this message has been deleted.
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

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
     * Gets the unique identifier of the message.
     *
     * @return the message ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the message.
     *
     * @param id the message ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the chat room ID this message belongs to.
     *
     * @return the chat room ID
     */
    public Integer getChatRoomId() {
        return chatRoomId;
    }

    /**
     * Sets the chat room ID this message belongs to.
     *
     * @param chatRoomId the chat room ID to set
     */
    public void setChatRoomId(Integer chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    /**
     * Gets the sender ID of the message.
     *
     * @return the sender ID
     */
    public Integer getSenderId() {
        return senderId;
    }

    /**
     * Sets the sender ID of the message.
     *
     * @param senderId the sender ID to set
     */
    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the text content of the message.
     *
     * @return the message text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the message.
     *
     * @param text the message text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the timestamp when the message was created.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the message was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the message was last edited.
     *
     * @return the edit timestamp, or null if never edited
     */
    public Instant getEditedAt() {
        return editedAt;
    }

    /**
     * Sets the timestamp when the message was last edited.
     *
     * @param editedAt the edit timestamp to set
     */
    public void setEditedAt(Instant editedAt) {
        this.editedAt = editedAt;
    }

    /**
     * Gets the timestamp when the message was deleted.
     *
     * @return the deletion timestamp, or null if not deleted
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * Sets the timestamp when the message was deleted.
     *
     * @param deletedAt the deletion timestamp to set
     */
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Gets the ID of the user who deleted this message.
     *
     * @return the deleter's user ID, or null if not deleted
     */
    public Integer getDeletedBy() {
        return deletedBy;
    }

    /**
     * Sets the ID of the user who deleted this message.
     *
     * @param deletedBy the deleter's user ID to set
     */
    public void setDeletedBy(Integer deletedBy) {
        this.deletedBy = deletedBy;
    }

    /**
     * Checks if the message has been edited.
     *
     * @return true if the message has been edited, false otherwise
     */
    public boolean isEdited() {
        return edited;
    }

    /**
     * Sets whether the message has been edited.
     *
     * @param edited true if the message has been edited, false otherwise
     */
    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    /**
     * Checks if the message has been deleted.
     *
     * @return true if the message has been deleted, false otherwise
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets whether the message has been deleted.
     *
     * @param deleted true if the message has been deleted, false otherwise
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
