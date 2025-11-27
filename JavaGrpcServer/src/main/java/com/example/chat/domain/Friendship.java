package com.example.chat.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "friendships")
public class Friendship {

    @EmbeddedId
    private FriendshipId id = new FriendshipId();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", insertable = false, updatable = false)
    private User friend;

    @Column(nullable = false)
    private Instant createdAt;

    public Friendship() {
    }

    public Friendship(User user, User friend) {
        this.user = user;
        this.friend = friend;
        this.id = new FriendshipId(user.getId(), friend.getId());
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public FriendshipId getId() {
        return id;
    }

    public void setId(FriendshipId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

