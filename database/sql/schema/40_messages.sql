CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT GENERATED ALWAYS AS IDENTITY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type VARCHAR(10) NOT NULL DEFAULT 'TEXT',
    body_text TEXT,
    ciphertext BYTEA,
    nonce BYTEA,
    encryption_algorithm VARCHAR(80),
    key_version INTEGER,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    edited_at TIMESTAMPTZ(6),
    deleted_at TIMESTAMPTZ,
    search_vector TSVECTOR GENERATED ALWAYS AS (
      to_tsvector('simple', coalesce(body_text, ''))
    ) STORED,
    PRIMARY KEY (message_id),
    CONSTRAINT chk_message_type CHECK (message_type IN ('TEXT','SYSTEM')),
    CONSTRAINT fk_message_sender_membership FOREIGN KEY (conversation_id, sender_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_message_content CHECK (
      (ciphertext IS NULL AND body_text IS NOT NULL AND nonce IS NULL AND key_version IS NULL)
      OR (ciphertext IS NOT NULL AND body_text IS NULL AND nonce IS NOT NULL AND encryption_algorithm IS NOT NULL AND key_version IS NOT NULL)
    ),
    CONSTRAINT chk_message_edit_time CHECK (edited_at IS NULL OR edited_at >= created_at),
    CONSTRAINT chk_message_delete_time CHECK (deleted_at IS NULL OR deleted_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_time ON messages(conversation_id, created_at, message_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_time ON messages(sender_id, created_at);
CREATE INDEX IF NOT EXISTS idx_messages_search ON messages USING GIN(search_vector);

CREATE TABLE IF NOT EXISTS message_hidden_for (
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    hidden_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (message_id, user_id),
    CONSTRAINT fk_hidden_message FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_hidden_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_hidden_user ON message_hidden_for(user_id, hidden_at);

CREATE TABLE IF NOT EXISTS conversation_clear_markers (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    cleared_at TIMESTAMPTZ(6) NOT NULL,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_clear_member FOREIGN KEY (conversation_id, user_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
