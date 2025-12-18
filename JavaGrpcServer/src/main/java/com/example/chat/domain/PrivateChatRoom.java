package com.example.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a private chat room between two users.
 * This class defines a one-to-one conversation between exactly two participants.
 */
@Entity
@Table(name = "private_chat_rooms")
public class PrivateChatRoom {

    /**
     * The unique identifier of this private chat room.
     * This ID corresponds to the associated ChatRoom entity.
     */
    @Id
    @Column(name = "chat_room_id")
    private Integer id;

    /**
     * The associated ChatRoom entity.
     * This relationship allows the private chat to inherit basic chat room properties.
     */
    @OneToOne(cascade = {})
    @MapsId
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    /**
     * The first user in the private chat.
     * By convention, this should be the user with the lower ID.
     */
    @ManyToOne
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    /**
     * The second user in the private chat.
     * By convention, this should be the user with the higher ID.
     */
    @ManyToOne
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    /**
     * Gets the unique identifier of the private chat room.
     *
     * @return the private chat room ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the private chat room.
     *
     * @param id the private chat room ID to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the associated ChatRoom entity.
     *
     * @return the chat room
     */
    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    /**
     * Sets the associated ChatRoom entity.
     *
     * @param chatRoom the chat room to set
     */
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * Gets the first user in the private chat.
     *
     * @return user A
     */
    public User getUserA() {
        return userA;
    }

    /**
     * Sets the first user in the private chat.
     *
     * @param userA the user to set as user A
     */
    public void setUserA(User userA) {
        this.userA = userA;
    }

    /**
     * Gets the second user in the private chat.
     *
     * @return user B
     */
    public User getUserB() {
        return userB;
    }

    /**
     * Sets the second user in the private chat.
     *
     * @param userB the user to set as user B
     */
    public void setUserB(User userB) {
        this.userB = userB;
    }
}

