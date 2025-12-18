package com.example.chat.domain;

/**
 * Enumeration representing the role of a user in a chat room.
 */
public enum MembershipRole {
    /**
     * The owner of the chat room with full administrative privileges.
     * Can promote/demote members, add/remove members, and delete the room.
     */
    OWNER,

    /**
     * An administrator with elevated privileges.
     * Can add/remove members and moderate content.
     */
    ADMIN,

    /**
     * A regular member with standard chat permissions.
     */
    MEMBER
}

