package com.example.chat.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FriendshipId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "friend_id")
    private Integer friendId;

    public FriendshipId() {
    }

    public FriendshipId(Integer userId, Integer friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFriendId() {
        return friendId;
    }

    public void setFriendId(Integer friendId) {
        this.friendId = friendId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipId that = (FriendshipId) o;
        return userId.equals(that.userId) && friendId.equals(that.friendId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() * 31 + friendId.hashCode();
    }
}

