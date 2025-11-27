-- Migration Script: Convert UUID columns to INTEGER (SERIAL)
-- WARNING: This will DELETE ALL EXISTING DATA
-- Run this as a PostgreSQL superuser connected to the chatdb database

-- Drop all existing tables (this deletes all data!)
DROP TABLE IF EXISTS message_audit CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS chat_room_memberships CASCADE;
DROP TABLE IF EXISTS group_chat_rooms CASCADE;
DROP TABLE IF EXISTS private_chat_rooms CASCADE;
DROP TABLE IF EXISTS chat_rooms CASCADE;
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS friend_requests CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop sequences if they exist
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS friend_requests_id_seq CASCADE;
DROP SEQUENCE IF EXISTS chat_rooms_id_seq CASCADE;
DROP SEQUENCE IF EXISTS chat_room_memberships_id_seq CASCADE;
DROP SEQUENCE IF EXISTS messages_id_seq CASCADE;
DROP SEQUENCE IF EXISTS message_audit_id_seq CASCADE;

-- Now recreate tables with INTEGER IDs (from setup_postgresql.sql)

-- Users
CREATE TABLE users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Friend Requests
CREATE TABLE friend_requests (
    id              SERIAL PRIMARY KEY,
    sender_id       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id     INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    responded_at    TIMESTAMPTZ,
    CONSTRAINT chk_friend_request_pair CHECK (sender_id <> receiver_id)
);

-- Create partial unique index for pending requests
CREATE UNIQUE INDEX uq_friend_request_pending 
    ON friend_requests(sender_id, receiver_id) 
    WHERE status = 'PENDING';

-- Friendships
CREATE TABLE friendships (
    id              SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_friendship_pair CHECK (user_id <> friend_id),
    CONSTRAINT uq_friendship UNIQUE (user_id, friend_id)
);

-- Chat Rooms
CREATE TABLE chat_rooms (
    id              SERIAL PRIMARY KEY,
    room_type       VARCHAR(20) NOT NULL, -- PRIVATE or GROUP
    owner_id        INT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Private Chat Rooms (inherits ChatRoom)
CREATE TABLE private_chat_rooms (
    chat_room_id    INT PRIMARY KEY REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_a_id       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_b_id       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_private_pair CHECK (user_a_id <> user_b_id)
);

-- Create unique index for private chat pairs (ensures only one chat per user pair)
CREATE UNIQUE INDEX uq_private_pair ON private_chat_rooms(
    LEAST(user_a_id, user_b_id), 
    GREATEST(user_a_id, user_b_id)
);

-- Group Chat Rooms
CREATE TABLE group_chat_rooms (
    chat_room_id    INT PRIMARY KEY REFERENCES chat_rooms(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    is_private      BOOLEAN NOT NULL DEFAULT FALSE
);

-- Chat Room Members
CREATE TABLE chat_room_memberships (
    id              SERIAL PRIMARY KEY,
    chat_room_id    INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id         INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- MEMBER / ADMIN / OWNER
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_membership UNIQUE (chat_room_id, user_id)
);

-- Messages
CREATE TABLE messages (
    id              SERIAL PRIMARY KEY,
    chat_room_id    INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id       INT NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    text            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    edited_at       TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    deleted_by      INT REFERENCES users(id) ON DELETE SET NULL,
    is_edited       BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_room ON messages(chat_room_id, created_at);
CREATE INDEX IF NOT EXISTS idx_messages_fulltext ON messages USING GIN (to_tsvector('english', text));

-- Message Edit History (optional audit)
CREATE TABLE message_audit (
    id              BIGSERIAL PRIMARY KEY,
    message_id      INT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    previous_content TEXT NOT NULL,
    edited_by       INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    edited_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Helper views
CREATE OR REPLACE VIEW user_friends AS
SELECT f.user_id, f.friend_id
FROM friendships f
UNION
SELECT f.friend_id AS user_id, f.user_id AS friend_id
FROM friendships f;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO chatuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO chatuser;

