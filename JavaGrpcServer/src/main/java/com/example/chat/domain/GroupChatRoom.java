package com.example.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_chat_rooms")
public class GroupChatRoom {

    @Id
    @Column(name = "chat_room_id")
    private Integer chatRoomId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "is_private", nullable = false)
    private boolean privateRoom = false;

    public Integer getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Integer chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public void setPrivateRoom(boolean privateRoom) {
        this.privateRoom = privateRoom;
    }
}

