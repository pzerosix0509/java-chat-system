CREATE TABLE IF NOT EXISTS conversations (
    conversation_id BIGINT GENERATED ALWAYS AS IDENTITY,
    conversation_type VARCHAR(10) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    CONSTRAINT chk_conversation_type CHECK (conversation_type IN ('DIRECT','GROUP')),
    CONSTRAINT fk_conversation_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_conversations_type_created ON conversations(conversation_type, created_at);

CREATE TABLE IF NOT EXISTS direct_conversations (
    conversation_id BIGINT NOT NULL,
    user_low_id BIGINT NOT NULL,
    user_high_id BIGINT NOT NULL,
    PRIMARY KEY (conversation_id),
    CONSTRAINT uq_direct_pair UNIQUE (user_low_id, user_high_id),
    CONSTRAINT fk_direct_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_direct_low FOREIGN KEY (user_low_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_direct_high FOREIGN KEY (user_high_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_direct_pair_order CHECK (user_low_id < user_high_id)
);

CREATE TABLE IF NOT EXISTS chat_groups (
    conversation_id BIGINT NOT NULL,
    group_name VARCHAR(150) NOT NULL,
    created_by BIGINT NOT NULL,
    encryption_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    current_key_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    CONSTRAINT fk_group_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_group_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_group_key_version CHECK ((encryption_enabled = FALSE AND current_key_version = 0) OR (encryption_enabled = TRUE AND current_key_version >= 1))
);

CREATE INDEX IF NOT EXISTS idx_groups_name ON chat_groups(group_name);
CREATE INDEX IF NOT EXISTS idx_groups_created ON chat_groups(created_at);

CREATE TABLE IF NOT EXISTS conversation_members (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_role VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    left_at TIMESTAMPTZ(6),
    added_by BIGINT,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT chk_member_role CHECK (member_role IN ('MEMBER','ADMIN')),
    CONSTRAINT fk_member_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_member_added_by FOREIGN KEY (added_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE RESTRICT,
    CONSTRAINT chk_member_times CHECK (left_at IS NULL OR left_at >= joined_at)
);

CREATE INDEX IF NOT EXISTS idx_members_user_active ON conversation_members(user_id, left_at, conversation_id);
CREATE INDEX IF NOT EXISTS idx_members_conversation_role ON conversation_members(conversation_id, member_role, left_at);

CREATE TABLE IF NOT EXISTS conversation_key_envelopes (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    key_version INTEGER NOT NULL,
    key_algorithm VARCHAR(80) NOT NULL,
    encrypted_key BYTEA NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id, user_id, key_version),
    CONSTRAINT fk_key_member FOREIGN KEY (conversation_id, user_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_key_envelopes_user ON conversation_key_envelopes(user_id, conversation_id, key_version);

DROP TRIGGER IF EXISTS trg_conversations_updated_at ON conversations;
CREATE TRIGGER trg_conversations_updated_at
BEFORE UPDATE ON conversations
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_chat_groups_updated_at ON chat_groups;
CREATE TRIGGER trg_chat_groups_updated_at
BEFORE UPDATE ON chat_groups
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
