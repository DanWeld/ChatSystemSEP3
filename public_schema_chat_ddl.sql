create schema if not exists public;
set search_path to public;
-- Remove objects if they exist
DROP TABLE IF EXISTS public.message_audit CASCADE;
DROP TABLE IF EXISTS public.messages CASCADE;
DROP TABLE IF EXISTS public.message CASCADE;
DROP TABLE IF EXISTS public.chat_room_memberships CASCADE;
DROP TABLE IF EXISTS public.private_chat_rooms CASCADE;
DROP TABLE IF EXISTS public.chat_rooms CASCADE;
DROP TABLE IF EXISTS public.friend_requests CASCADE;
DROP TABLE IF EXISTS public.friendships CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;

-- Users table
CREATE TABLE public.users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Chat rooms (group / public rooms)
CREATE TABLE public.chat_rooms (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(255),
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Private (one-to-one) chat rooms table
CREATE TABLE public.private_chat_rooms (
    id              SERIAL PRIMARY KEY,
    user_one_id     INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    user_two_id     INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT private_chat_no_self CHECK (user_one_id <> user_two_id)
);

-- Chat room memberships (users in rooms)
CREATE TABLE public.chat_room_memberships (
    id              SERIAL PRIMARY KEY,
    chat_room_id    INTEGER NOT NULL REFERENCES public.chat_rooms(id) ON DELETE CASCADE,
    user_id         INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    role            VARCHAR(50) DEFAULT 'member',
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_room_user UNIQUE (chat_room_id, user_id)
);

-- Main messages table
CREATE TABLE public.messages (
    id              SERIAL PRIMARY KEY,
    chat_room_id    INTEGER NOT NULL REFERENCES public.chat_rooms(id) ON DELETE CASCADE,
    sender_id       INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    text            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    edited_at       TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    deleted_by      INTEGER REFERENCES public.users(id) ON DELETE SET NULL,
    is_edited       BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

--alternative message table
CREATE TABLE public.message (
    id              SERIAL PRIMARY KEY,
    chat_room_id    INTEGER,
    user_id         INTEGER,
    text            TEXT,
    timestamp       TIMESTAMPTZ DEFAULT now(),
    is_edited       BOOLEAN DEFAULT FALSE,
    is_deleted      BOOLEAN DEFAULT FALSE
);

-- Message audit/history table
CREATE TABLE public.message_audit (
    id              SERIAL PRIMARY KEY,
    message_id      INTEGER NOT NULL REFERENCES public.messages(id) ON DELETE CASCADE,
    action          VARCHAR(100) NOT NULL, -- e.g. 'edited','deleted','restored'
    acted_by        INTEGER REFERENCES public.users(id) ON DELETE SET NULL,
    acted_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    details         TEXT
);

-- Friend requests and friendships (as seen in screenshot)
CREATE TABLE public.friend_requests (
    id              SERIAL PRIMARY KEY,
    sender_user_id  INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    receiver_user_id INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, accepted, denied
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at    TIMESTAMPTZ
);

CREATE TABLE public.friendships (
    id              SERIAL PRIMARY KEY,
    user_a_id       INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    user_b_id       INTEGER NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT friendship_order CHECK (user_a_id <> user_b_id),
    CONSTRAINT friendship_pair_uniq UNIQUE (LEAST(user_a_id, user_b_id), GREATEST(user_a_id, user_b_id))
);

-- Indexes (helpful for common queries)
CREATE INDEX IF NOT EXISTS idx_messages_chat_room_created_at ON public.messages(chat_room_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON public.messages(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chat_room_memberships_user ON public.chat_room_memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_private_chat_users ON public.private_chat_rooms(user_one_id, user_two_id);

CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver ON public.friend_requests(receiver_user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_users ON public.friendships(user_a_id, user_b_id);

-- Optional: small view to show active friends (bidirectional)
CREATE OR REPLACE VIEW public.user_friends AS
SELECT user_a_id AS user_id, user_b_id AS friend_id, created_at
FROM public.friendships
UNION
SELECT user_b_id AS user_id, user_a_id AS friend_id, created_at
FROM public.friendships;


