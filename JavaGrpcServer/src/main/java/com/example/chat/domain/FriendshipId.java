package com.example.chat.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Composite primary key for the Friendship entity.
 * This class combines user ID and friend ID to create a unique identifier
 * for friendship relationships.
 */
@Embeddable
public class FriendshipId implements Serializable {

    /**
     * The ID of the user who owns the friendship record.
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * The ID of the friend in the friendship relationship.
     */
    @Column(name = "friend_id")
    private Integer friendId;

    /**
     * Default constructor required by JPA.
     */
    public FriendshipId() {
    }

    /**
     * Constructs a FriendshipId with the specified user and friend IDs.
     *
     * @param userId the user ID
     * @param friendId the friend ID
     */
    public FriendshipId(Integer userId, Integer friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Gets the friend ID.
     *
     * @return the friend ID
     */
    public Integer getFriendId() {
        return friendId;
    }

    /**
     * Sets the friend ID.
     *
     * @param friendId the friend ID to set
     */
    public void setFriendId(Integer friendId) {
        this.friendId = friendId;
    }

    /**
     * Compares this FriendshipId with another object for equality.
     * Two FriendshipIds are equal if both their user IDs and friend IDs match.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipId that = (FriendshipId) o;
        return userId.equals(that.userId) && friendId.equals(that.friendId);
    }

    /**
     * Generates a hash code for this FriendshipId.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return userId.hashCode() * 31 + friendId.hashCode();
    }
}

