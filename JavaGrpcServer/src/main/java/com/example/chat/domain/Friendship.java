package com.example.chat.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity representing a friendship relationship between two users.
 * Friendships are bidirectional, meaning if user A is friends with user B,
 * there should be two Friendship records (A->B and B->A).
 */
@Entity
@Table(name = "friendships")
public class Friendship {

    /**
     * Composite primary key consisting of user ID and friend ID.
     */
    @EmbeddedId
    private FriendshipId id = new FriendshipId();

    /**
     * The user who owns this friendship record.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * The friend in this friendship relationship.
     */
    @ManyToOne
    @JoinColumn(name = "friend_id", insertable = false, updatable = false)
    private User friend;

    /**
     * Timestamp when the friendship was established.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Default constructor required by JPA.
     */
    public Friendship() {
    }

    /**
     * Constructs a new Friendship between two users.
     *
     * @param user the user who owns this friendship record
     * @param friend the friend in this relationship
     */
    public Friendship(User user, User friend) {
        this.user = user;
        this.friend = friend;
        this.id = new FriendshipId(user.getId(), friend.getId());
    }

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
     * Gets the composite ID of this friendship.
     *
     * @return the friendship ID
     */
    public FriendshipId getId() {
        return id;
    }

    /**
     * Sets the composite ID of this friendship.
     *
     * @param id the friendship ID to set
     */
    public void setId(FriendshipId id) {
        this.id = id;
    }

    /**
     * Gets the user who owns this friendship record.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who owns this friendship record.
     *
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the friend in this friendship relationship.
     *
     * @return the friend
     */
    public User getFriend() {
        return friend;
    }

    /**
     * Sets the friend in this friendship relationship.
     *
     * @param friend the friend to set
     */
    public void setFriend(User friend) {
        this.friend = friend;
    }

    /**
     * Gets the timestamp when the friendship was established.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the friendship was established.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

