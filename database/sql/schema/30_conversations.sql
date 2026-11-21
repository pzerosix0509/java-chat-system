USE chat_app_db;

CREATE TABLE IF NOT EXISTS conversations (
    conversation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_type ENUM('DIRECT','GROUP') NOT NULL,
    created_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    KEY idx_conversations_type_created (conversation_type, created_at),
    CONSTRAINT fk_conversation_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS direct_conversations (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_low_id BIGINT UNSIGNED NOT NULL,
    user_high_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (conversation_id),
    UNIQUE KEY uq_direct_pair (user_low_id, user_high_id),
    CONSTRAINT fk_direct_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_direct_low FOREIGN KEY (user_low_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_direct_high FOREIGN KEY (user_high_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_direct_pair_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chat_groups (
    conversation_id BIGINT UNSIGNED NOT NULL,
    group_name VARCHAR(150) NOT NULL,
    created_by BIGINT UNSIGNED NOT NULL,
    encryption_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    current_key_version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    KEY idx_groups_name (group_name),
    KEY idx_groups_created (created_at),
    CONSTRAINT fk_group_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_group_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_group_key_version CHECK ((encryption_enabled = FALSE AND current_key_version = 0) OR (encryption_enabled = TRUE AND current_key_version >= 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_members (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    member_role ENUM('MEMBER','ADMIN') NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    left_at DATETIME(6) NULL,
    added_by BIGINT UNSIGNED NULL,
    PRIMARY KEY (conversation_id, user_id),
    KEY idx_members_user_active (user_id, left_at, conversation_id),
    KEY idx_members_conversation_role (conversation_id, member_role, left_at),
    CONSTRAINT fk_member_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_member_added_by FOREIGN KEY (added_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_member_times CHECK (left_at IS NULL OR left_at >= joined_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_key_envelopes (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    key_version INT UNSIGNED NOT NULL,
    key_algorithm VARCHAR(80) NOT NULL,
    encrypted_key MEDIUMBLOB NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id, user_id, key_version),
    KEY idx_key_envelopes_user (user_id, conversation_id, key_version),
    CONSTRAINT fk_key_member FOREIGN KEY (conversation_id, user_id) REFERENCES conversation_members(conversation_id, user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
