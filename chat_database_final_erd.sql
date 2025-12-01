
-- Create schema
CREATE SCHEMA IF NOT EXISTS chatsystem;

-- Set schema
SET search_path TO chatsystem, public;


-- USER TABLE
-- ============================================================================
CREATE TABLE chatsystem.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- CHATROOM TABLE
-- type = 'private' or 'group'
-- groupName nullable for private chats
-- ============================================================================
CREATE TABLE chatsystem.chatroom (
    id SERIAL PRIMARY KEY,
    type VARCHAR(10) NOT NULL CHECK (type IN ('private', 'group')),
    groupName VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    
    -- Ensure private chats don't have groupName
    CHECK (
        (type = 'private' AND groupName IS NULL)
        OR (type = 'group')
    )
);

-- USER-CHATROOM LINK TABLE (Junction Table)
-- ============================================================================
CREATE TABLE chatsystem.userchatroominfo (
    userId INTEGER NOT NULL REFERENCES chatsystem.users(id) ON DELETE CASCADE,
    chatRoomId INTEGER NOT NULL REFERENCES chatsystem.chatroom(id) ON DELETE CASCADE,
    role VARCHAR(30) DEFAULT 'member' CHECK (role IN ('member', 'admin', 'owner')),
    joinDate TIMESTAMP DEFAULT NOW(),
    
    PRIMARY KEY (userId, chatRoomId)
);

-- MESSAGE TABLE
-- ============================================================================
CREATE TABLE chatsystem.message (
    id SERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    userId INTEGER NOT NULL REFERENCES chatsystem.users(id) ON DELETE CASCADE,
    chatRoomId INTEGER NOT NULL REFERENCES chatsystem.chatroom(id) ON DELETE CASCADE,
    is_edited BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- FRIEND_INFO TABLE
-- requestStatus = pending | accept | deny
-- isFriend = TRUE only when accepted
-- ============================================================================
CREATE TABLE chatsystem.friend_info (
    request_id SERIAL PRIMARY KEY,
    senderUserId INTEGER NOT NULL REFERENCES chatsystem.users(id) ON DELETE CASCADE,
    receiverUserId INTEGER NOT NULL REFERENCES chatsystem.users(id) ON DELETE CASCADE,
    requestStatus VARCHAR(10) NOT NULL CHECK (requestStatus IN ('pending', 'accept', 'deny')),
    isFriend BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    responded_at TIMESTAMP,
    
    -- Prevent sending friend request to oneself
    CHECK (senderUserId <> receiverUserId),
    
    -- Ensure isFriend is only true when status is accept
    CHECK (
        (requestStatus = 'accept' AND isFriend = TRUE)
        OR (requestStatus != 'accept' AND isFriend = FALSE)
    )
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- User indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON chatsystem.users(username);

-- Chatroom indexes
CREATE INDEX IF NOT EXISTS idx_chatroom_type ON chatsystem.chatroom(type);
CREATE INDEX IF NOT EXISTS idx_chatroom_groupname ON chatsystem.chatroom(groupName) WHERE groupName IS NOT NULL;

-- User-Chatroom link indexes
CREATE INDEX IF NOT EXISTS idx_userchatroominfo_userId ON chatsystem.userchatroominfo(userId);
CREATE INDEX IF NOT EXISTS idx_userchatroominfo_chatRoomId ON chatsystem.userchatroominfo(chatRoomId);
CREATE INDEX IF NOT EXISTS idx_userchatroominfo_role ON chatsystem.userchatroominfo(role);

-- Message indexes
CREATE INDEX IF NOT EXISTS idx_message_chatRoomId ON chatsystem.message(chatRoomId, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_message_userId ON chatsystem.message(userId);
CREATE INDEX IF NOT EXISTS idx_message_timestamp ON chatsystem.message(timestamp DESC);

-- Friend info indexes
CREATE INDEX IF NOT EXISTS idx_friend_info_sender ON chatsystem.friend_info(senderUserId);
CREATE INDEX IF NOT EXISTS idx_friend_info_receiver ON chatsystem.friend_info(receiverUserId);
CREATE INDEX IF NOT EXISTS idx_friend_info_status ON chatsystem.friend_info(requestStatus);
CREATE INDEX IF NOT EXISTS idx_friend_info_isfriend ON chatsystem.friend_info(isFriend) WHERE isFriend = TRUE;

-- Unique constraint for pending friend requests (prevent duplicate pending requests)
CREATE UNIQUE INDEX IF NOT EXISTS idx_friend_info_pending_unique 
    ON chatsystem.friend_info(senderUserId, receiverUserId) 
    WHERE requestStatus = 'pending';

-- ============================================================================
-- HELPER VIEWS
-- ============================================================================

-- View for active friendships (bidirectional)
CREATE OR REPLACE VIEW chatsystem.user_friends AS
SELECT 
    senderUserId AS user_id,
    receiverUserId AS friend_id,
    created_at
FROM chatsystem.friend_info
WHERE isFriend = TRUE
UNION
SELECT 
    receiverUserId AS user_id,
    senderUserId AS friend_id,
    created_at
FROM chatsystem.friend_info
WHERE isFriend = TRUE;

-- View for pending friend requests
CREATE OR REPLACE VIEW chatsystem.pending_friend_requests AS
SELECT 
    fr.request_id,
    fr.senderUserId,
    u1.username AS sender_username,
    fr.receiverUserId,
    u2.username AS receiver_username,
    fr.requestStatus,
    fr.created_at
FROM chatsystem.friend_info fr
JOIN chatsystem.users u1 ON fr.senderUserId = u1.id
JOIN chatsystem.users u2 ON fr.receiverUserId = u2.id
WHERE fr.requestStatus = 'pending';

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================

