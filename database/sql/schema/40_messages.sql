USE chat_app_db;

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED NOT NULL,
    message_type ENUM('TEXT','SYSTEM') NOT NULL DEFAULT 'TEXT',
    body_text TEXT NULL,
    ciphertext MEDIUMBLOB NULL,
    nonce VARBINARY(32) NULL,
    encryption_algorithm VARCHAR(80) NULL,
    key_version INT UNSIGNED NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    edited_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (message_id),
    KEY idx_messages_conversation_time (conversation_id, created_at, message_id),
    KEY idx_messages_sender_time (sender_id, created_at),
    FULLTEXT KEY ft_messages_body (body_text),
    CONSTRAINT fk_message_sender_membership FOREIGN KEY (conversation_id, sender_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_message_content CHECK (
      (ciphertext IS NULL AND body_text IS NOT NULL AND nonce IS NULL AND key_version IS NULL)
      OR (ciphertext IS NOT NULL AND body_text IS NULL AND nonce IS NOT NULL AND encryption_algorithm IS NOT NULL AND key_version IS NOT NULL)
    ),
    CONSTRAINT chk_message_edit_time CHECK (edited_at IS NULL OR edited_at >= created_at),
    CONSTRAINT chk_message_delete_time CHECK (deleted_at IS NULL OR deleted_at >= created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message_hidden_for (
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    hidden_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (message_id, user_id),
    KEY idx_hidden_user (user_id, hidden_at),
    CONSTRAINT fk_hidden_message FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hidden_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_clear_markers (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    cleared_at DATETIME(6) NOT NULL,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_clear_member FOREIGN KEY (conversation_id, user_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
