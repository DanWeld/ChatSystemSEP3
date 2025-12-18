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
 * Entity representing a membership of a user in a chat room.
 * This class defines the relationship between users and chat rooms,
 * including the role of the user within the chat room and the timestamp of joining.
 */
@Entity
@Table(name = "chat_room_memberships")
public class ChatRoomMembership {

    /**
     * Unique identifier for the chat room membership.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The chat room this membership belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * The user who is a member of the chat room.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The role of the user in the chat room (e.g., OWNER, ADMIN, MEMBER).
     * Defaults to MEMBER.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role = MembershipRole.MEMBER;

    /**
     * Timestamp when the user joined the chat room.
     */
    @Column(nullable = false)
    private Instant joinedAt;

    /**
     * Pre-persist lifecycle callback that sets the joinedAt timestamp
     * to the current time if it hasn't been set yet.
     */
    @PrePersist
    public void prePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    /**
     * Gets the unique identifier of the membership.
     *
     * @return the membership ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the membership.
     *
     * @param id the membership ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the chat room this membership belongs to.
     *
     * @return the chat room
     */
    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    /**
     * Sets the chat room this membership belongs to.
     *
     * @param chatRoom the chat room to set
     */
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * Gets the user who is a member of the chat room.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who is a member of the chat room.
     *
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the role of the user in the chat room.
     *
     * @return the membership role
     */
    public MembershipRole getRole() {
        return role;
    }

    /**
     * Sets the role of the user in the chat room.
     *
     * @param role the membership role to set
     */
    public void setRole(MembershipRole role) {
        this.role = role;
    }

    /**
     * Gets the timestamp when the user joined the chat room.
     *
     * @return the join timestamp
     */
    public Instant getJoinedAt() {
        return joinedAt;
    }

    /**
     * Sets the timestamp when the user joined the chat room.
     *
     * @param joinedAt the join timestamp to set
     */
    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}

