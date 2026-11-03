-- ============================================================
-- CHAT APPLICATION DATABASE
-- Target: MySQL 8.0.16+ (InnoDB, utf8mb4)
-- Scope: Admin module + End-user module
-- Notes:
--   * Passwords are stored as Argon2id/bcrypt hashes, never plaintext.
--   * A user is soft-deleted by setting status='DELETED' and deleted_at.
--   * Direct and group conversations share the conversations/messages model.
--   * Group E2EE stores ciphertext and per-member encrypted key envelopes.
--   * The application must enforce authorization and rotate group keys.
-- ============================================================

CREATE DATABASE IF NOT EXISTS chat_app_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE chat_app_db;

-- ------------------------------------------------------------
-- 1. USERS / AUTHENTICATION
-- ------------------------------------------------------------
-- System roles are separate from chat-group roles (conversation_members.member_role).
CREATE TABLE IF NOT EXISTS roles (
    role_id       SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    role_code     VARCHAR(30) NOT NULL,
    role_name     VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uq_roles_code (role_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    user_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    address         VARCHAR(255) NULL,
    birth_date      DATE NULL,
    gender          ENUM('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY')
                    NOT NULL DEFAULT 'PREFER_NOT_TO_SAY',
    email           VARCHAR(254) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status          ENUM('ACTIVE','LOCKED','DELETED') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                    ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6) NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_full_name (full_name),
    KEY idx_users_status_created (status, created_at),
    KEY idx_users_created_at (created_at),
    CONSTRAINT chk_users_deleted_at
      CHECK ((status = 'DELETED' AND deleted_at IS NOT NULL)
          OR (status <> 'DELETED' AND deleted_at IS NULL))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id       BIGINT UNSIGNED NOT NULL,
    role_id       SMALLINT UNSIGNED NOT NULL,
    assigned_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    assigned_by   BIGINT UNSIGNED NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role (role_id, user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id)
      REFERENCES roles(role_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_user_role_assigner FOREIGN KEY (assigned_by)
      REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

INSERT IGNORE INTO roles(role_code, role_name) VALUES
  ('USER', 'Người sử dụng'),
  ('ADMIN', 'Quản trị viên');

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    reset_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED NOT NULL,
    token_hash      CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at      DATETIME(6) NOT NULL,
    used_at         DATETIME(6) NULL,
    PRIMARY KEY (reset_id),
    UNIQUE KEY uq_password_reset_token_hash (token_hash),
    KEY idx_reset_user_expiry (user_id, expires_at, used_at),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_reset_expiry CHECK (expires_at > created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS login_history (
    login_id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id             BIGINT UNSIGNED NOT NULL,
    username_snapshot   VARCHAR(50) NOT NULL,
    full_name_snapshot  VARCHAR(150) NOT NULL,
    login_status        ENUM('SUCCESS','FAILED','LOCKED') NOT NULL,
    occurred_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address          VARCHAR(45) NULL,
    user_agent          VARCHAR(500) NULL,
    PRIMARY KEY (login_id),
    KEY idx_login_time (occurred_at),
    KEY idx_login_user_time (user_id, occurred_at),
    KEY idx_login_username_time (username_snapshot, occurred_at),
    CONSTRAINT fk_login_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- One row per app login/session. Online status is derived from last_seen_at.
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id      CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    started_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_seen_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ended_at        DATETIME(6) NULL,
    ip_address      VARCHAR(45) NULL,
    device_info     VARCHAR(500) NULL,
    PRIMARY KEY (session_id),
    KEY idx_sessions_user_started (user_id, started_at),
    KEY idx_sessions_presence (user_id, ended_at, last_seen_at),
    KEY idx_sessions_started (started_at),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_session_times
      CHECK (last_seen_at >= started_at AND (ended_at IS NULL OR ended_at >= started_at))
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. FRIENDS / FRIEND REQUESTS / BLOCKS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS friendships (
    user_low_id     BIGINT UNSIGNED NOT NULL,
    user_high_id    BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_low_id, user_high_id),
    KEY idx_friendships_high (user_high_id),
    CONSTRAINT fk_friend_low FOREIGN KEY (user_low_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_friend_high FOREIGN KEY (user_high_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS friend_requests (
    request_id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sender_id           BIGINT UNSIGNED NOT NULL,
    receiver_id         BIGINT UNSIGNED NOT NULL,
    status              ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED')
                        NOT NULL DEFAULT 'PENDING',
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    responded_at        DATETIME(6) NULL,
    -- These generated columns permit only one pending request for an unordered pair.
    pending_low_id      BIGINT UNSIGNED GENERATED ALWAYS AS
      (CASE WHEN status = 'PENDING' THEN LEAST(sender_id, receiver_id) ELSE NULL END) STORED,
    pending_high_id     BIGINT UNSIGNED GENERATED ALWAYS AS
      (CASE WHEN status = 'PENDING' THEN GREATEST(sender_id, receiver_id) ELSE NULL END) STORED,
    PRIMARY KEY (request_id),
    UNIQUE KEY uq_one_pending_request_per_pair (pending_low_id, pending_high_id),
    KEY idx_friend_req_receiver (receiver_id, status, created_at),
    KEY idx_friend_req_sender (sender_id, status, created_at),
    CONSTRAINT fk_friend_req_sender FOREIGN KEY (sender_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_friend_req_receiver FOREIGN KEY (receiver_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_friend_req_not_self CHECK (sender_id <> receiver_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_blocks (
    blocker_id      BIGINT UNSIGNED NOT NULL,
    blocked_id      BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (blocker_id, blocked_id),
    KEY idx_blocks_blocked (blocked_id, blocker_id),
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_block_not_self CHECK (blocker_id <> blocked_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. CONVERSATIONS / GROUPS / MEMBERS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversations (
    conversation_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_type   ENUM('DIRECT','GROUP') NOT NULL,
    created_by          BIGINT UNSIGNED NOT NULL,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    KEY idx_conversations_type_created (conversation_type, created_at),
    CONSTRAINT fk_conversation_creator FOREIGN KEY (created_by)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS direct_conversations (
    conversation_id     BIGINT UNSIGNED NOT NULL,
    user_low_id         BIGINT UNSIGNED NOT NULL,
    user_high_id        BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (conversation_id),
    UNIQUE KEY uq_direct_pair (user_low_id, user_high_id),
    CONSTRAINT fk_direct_conversation FOREIGN KEY (conversation_id)
      REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_direct_low FOREIGN KEY (user_low_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_direct_high FOREIGN KEY (user_high_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_direct_pair_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chat_groups (
    conversation_id     BIGINT UNSIGNED NOT NULL,
    group_name          VARCHAR(150) NOT NULL,
    created_by          BIGINT UNSIGNED NOT NULL,
    encryption_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    current_key_version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id),
    KEY idx_groups_name (group_name),
    KEY idx_groups_created (created_at),
    CONSTRAINT fk_group_conversation FOREIGN KEY (conversation_id)
      REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_group_creator FOREIGN KEY (created_by)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_group_key_version
      CHECK ((encryption_enabled = FALSE AND current_key_version = 0)
          OR (encryption_enabled = TRUE AND current_key_version >= 1))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS conversation_members (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    member_role     ENUM('MEMBER','ADMIN') NOT NULL DEFAULT 'MEMBER',
    joined_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    left_at         DATETIME(6) NULL,
    added_by        BIGINT UNSIGNED NULL,
    PRIMARY KEY (conversation_id, user_id),
    KEY idx_members_user_active (user_id, left_at, conversation_id),
    KEY idx_members_conversation_role (conversation_id, member_role, left_at),
    CONSTRAINT fk_member_conversation FOREIGN KEY (conversation_id)
      REFERENCES conversations(conversation_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_member_added_by FOREIGN KEY (added_by)
      REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_member_times CHECK (left_at IS NULL OR left_at >= joined_at)
) ENGINE=InnoDB;

-- Encrypted group key for each member and key version (key envelope, not plaintext key).
CREATE TABLE IF NOT EXISTS conversation_key_envelopes (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    key_version     INT UNSIGNED NOT NULL,
    key_algorithm   VARCHAR(80) NOT NULL,
    encrypted_key   MEDIUMBLOB NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (conversation_id, user_id, key_version),
    KEY idx_key_envelopes_user (user_id, conversation_id, key_version),
    CONSTRAINT fk_key_member FOREIGN KEY (conversation_id, user_id)
      REFERENCES conversation_members(conversation_id, user_id)
      ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. MESSAGES / PER-USER HISTORY DELETION
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    message_id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_id     BIGINT UNSIGNED NOT NULL,
    sender_id           BIGINT UNSIGNED NOT NULL,
    message_type        ENUM('TEXT','SYSTEM') NOT NULL DEFAULT 'TEXT',
    body_text           TEXT NULL,
    ciphertext          MEDIUMBLOB NULL,
    nonce               VARBINARY(32) NULL,
    encryption_algorithm VARCHAR(80) NULL,
    key_version         INT UNSIGNED NULL,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    edited_at           DATETIME(6) NULL,
    deleted_at          DATETIME(6) NULL,
    PRIMARY KEY (message_id),
    KEY idx_messages_conversation_time (conversation_id, created_at, message_id),
    KEY idx_messages_sender_time (sender_id, created_at),
    FULLTEXT KEY ft_messages_body (body_text),
    CONSTRAINT fk_message_sender_membership FOREIGN KEY (conversation_id, sender_id)
      REFERENCES conversation_members(conversation_id, user_id)
      ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_message_content CHECK (
      (ciphertext IS NULL AND body_text IS NOT NULL AND nonce IS NULL AND key_version IS NULL)
      OR
      (ciphertext IS NOT NULL AND body_text IS NULL AND nonce IS NOT NULL
       AND encryption_algorithm IS NOT NULL AND key_version IS NOT NULL)
    ),
    CONSTRAINT chk_message_edit_time CHECK (edited_at IS NULL OR edited_at >= created_at),
    CONSTRAINT chk_message_delete_time CHECK (deleted_at IS NULL OR deleted_at >= created_at)
) ENGINE=InnoDB;

-- Hide selected messages only for one user (does not erase the other participant's copy).
CREATE TABLE IF NOT EXISTS message_hidden_for (
    message_id  BIGINT UNSIGNED NOT NULL,
    user_id     BIGINT UNSIGNED NOT NULL,
    hidden_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (message_id, user_id),
    KEY idx_hidden_user (user_id, hidden_at),
    CONSTRAINT fk_hidden_message FOREIGN KEY (message_id)
      REFERENCES messages(message_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hidden_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Clear all history for a user in a conversation without deleting it for others.
CREATE TABLE IF NOT EXISTS conversation_clear_markers (
    conversation_id BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    cleared_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_clear_member FOREIGN KEY (conversation_id, user_id)
      REFERENCES conversation_members(conversation_id, user_id)
      ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. SPAM REPORTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS spam_reports (
    report_id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reporter_id         BIGINT UNSIGNED NOT NULL,
    reported_user_id    BIGINT UNSIGNED NOT NULL,
    message_id          BIGINT UNSIGNED NULL,
    reason              VARCHAR(120) NOT NULL DEFAULT 'SPAM',
    details             TEXT NULL,
    status              ENUM('OPEN','REVIEWED','DISMISSED','ACTIONED')
                        NOT NULL DEFAULT 'OPEN',
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reviewed_by         BIGINT UNSIGNED NULL,
    reviewed_at         DATETIME(6) NULL,
    PRIMARY KEY (report_id),
    KEY idx_reports_time (created_at),
    KEY idx_reports_reported_user (reported_user_id, created_at),
    KEY idx_reports_status_time (status, created_at),
    KEY idx_reports_reporter (reporter_id, created_at),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_message FOREIGN KEY (message_id)
      REFERENCES messages(message_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_report_reviewer FOREIGN KEY (reviewed_by)
      REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_report_not_self CHECK (reporter_id <> reported_user_id),
    CONSTRAINT chk_report_review_time CHECK (reviewed_at IS NULL OR reviewed_at >= created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. OPTIONAL LLM USAGE AUDIT (do not store private chat prompts by default)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS llm_usage_logs (
    usage_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED NOT NULL,
    feature         ENUM('REWRITE','SUGGEST_REPLY','OTHER') NOT NULL,
    provider        VARCHAR(80) NULL,
    model_name      VARCHAR(120) NULL,
    input_tokens    INT UNSIGNED NULL,
    output_tokens   INT UNSIGNED NULL,
    status          ENUM('SUCCESS','FAILED') NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (usage_id),
    KEY idx_llm_user_time (user_id, created_at),
    CONSTRAINT fk_llm_user FOREIGN KEY (user_id)
      REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. REPORTING VIEW: direct friends + distinct friends-of-friends
-- friends_of_friends_count excludes the user and their direct friends.
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_user_friend_counts AS
SELECT
    u.user_id,
    COALESCE(d.direct_friends_count, 0) AS direct_friends_count,
    COALESCE(f.friends_of_friends_count, 0) AS friends_of_friends_count,
    COALESCE(d.direct_friends_count, 0) + COALESCE(f.friends_of_friends_count, 0)
      AS network_size_including_friends_of_friends
FROM users u
LEFT JOIN (
    SELECT edge.user_id, COUNT(*) AS direct_friends_count
    FROM (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) edge
    GROUP BY edge.user_id
) d ON d.user_id = u.user_id
LEFT JOIN (
    SELECT first_edge.user_id, COUNT(DISTINCT second_edge.friend_id) AS friends_of_friends_count
    FROM (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) first_edge
    JOIN (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) second_edge ON second_edge.user_id = first_edge.friend_id
    LEFT JOIN (
        SELECT user_low_id AS user_id, user_high_id AS friend_id FROM friendships
        UNION ALL
        SELECT user_high_id AS user_id, user_low_id AS friend_id FROM friendships
    ) direct_edge
      ON direct_edge.user_id = first_edge.user_id
     AND direct_edge.friend_id = second_edge.friend_id
    WHERE second_edge.friend_id <> first_edge.user_id
      AND direct_edge.friend_id IS NULL
    GROUP BY first_edge.user_id
) f ON f.user_id = u.user_id
WHERE u.status <> 'DELETED';

-- ------------------------------------------------------------
-- 8. COMMON QUERY EXAMPLES
-- Replace @variables with parameters from your Java code (PreparedStatement).
-- ------------------------------------------------------------

-- [ADMIN 1] User list: filters by name / username / status; sort by name or created date.
-- SELECT user_id, username, full_name, email, status, created_at
-- FROM users
-- WHERE status <> 'DELETED'
--   AND (@keyword IS NULL OR full_name LIKE CONCAT('%', @keyword, '%')
--                         OR username LIKE CONCAT('%', @keyword, '%'))
--   AND (@status IS NULL OR status = @status)
-- ORDER BY full_name ASC, created_at DESC;

-- [ADMIN 2] Login history in time order.
-- SELECT occurred_at, username_snapshot AS username, full_name_snapshot AS full_name,
--        login_status, ip_address
-- FROM login_history
-- ORDER BY occurred_at DESC;

-- [ADMIN 3] Group list, member list and admin list.
-- SELECT c.conversation_id, g.group_name, g.created_at,
--        COUNT(CASE WHEN cm.left_at IS NULL THEN 1 END) AS active_member_count
-- FROM chat_groups g
-- JOIN conversations c ON c.conversation_id = g.conversation_id
-- LEFT JOIN conversation_members cm ON cm.conversation_id = g.conversation_id
-- WHERE (@group_name IS NULL OR g.group_name LIKE CONCAT('%', @group_name, '%'))
-- GROUP BY c.conversation_id, g.group_name, g.created_at
-- ORDER BY g.created_at DESC;
-- Members:
-- SELECT u.user_id, u.username, u.full_name, cm.member_role, cm.joined_at
-- FROM conversation_members cm JOIN users u ON u.user_id = cm.user_id
-- WHERE cm.conversation_id = @conversation_id AND cm.left_at IS NULL
-- ORDER BY cm.member_role DESC, u.full_name;
-- Admins: same query with AND cm.member_role = 'ADMIN'.

-- [ADMIN 4] Spam reports; optionally filter by time and reported username.
-- SELECT sr.report_id, sr.created_at, reporter.username AS reporter_username,
--        reported.username AS reported_username, sr.reason, sr.details, sr.status
-- FROM spam_reports sr
-- JOIN users reporter ON reporter.user_id = sr.reporter_id
-- JOIN users reported ON reported.user_id = sr.reported_user_id
-- WHERE (@from_time IS NULL OR sr.created_at >= @from_time)
--   AND (@to_time IS NULL OR sr.created_at < @to_time)
--   AND (@username IS NULL OR reported.username LIKE CONCAT('%', @username, '%'))
-- ORDER BY sr.created_at DESC;
-- Lock reported account after admin authorization:
-- UPDATE users SET status='LOCKED' WHERE user_id=@reported_user_id AND status='ACTIVE';

-- [ADMIN 5] Newly registered users in a date range.
-- SELECT user_id, username, full_name, created_at
-- FROM users
-- WHERE created_at >= @from_time AND created_at < @to_time
--   AND status <> 'DELETED'
--   AND (@name IS NULL OR full_name LIKE CONCAT('%', @name, '%'))
-- ORDER BY created_at DESC;

-- [ADMIN 6] Registrations by month for a selected year, including months with zero users.
-- WITH RECURSIVE months AS (
--   SELECT 1 AS month_no UNION ALL SELECT month_no + 1 FROM months WHERE month_no < 12
-- ), registration_counts AS (
--   SELECT MONTH(created_at) AS month_no, COUNT(*) AS total
--   FROM users
--   WHERE YEAR(created_at) = @selected_year AND status <> 'DELETED'
--   GROUP BY MONTH(created_at)
-- )
-- SELECT months.month_no, COALESCE(registration_counts.total, 0) AS registrations
-- FROM months LEFT JOIN registration_counts USING (month_no)
-- ORDER BY months.month_no;

-- [ADMIN 7] Direct friends and distinct friends-of-friends.
-- SELECT u.user_id, u.username, u.full_name,
--        fc.direct_friends_count, fc.network_size_including_friends_of_friends
-- FROM v_user_friend_counts fc JOIN users u ON u.user_id = fc.user_id
-- WHERE (@name IS NULL OR u.full_name LIKE CONCAT('%', @name, '%'))
--   AND (@min_direct IS NULL OR fc.direct_friends_count >= @min_direct)
--   AND (@max_direct IS NULL OR fc.direct_friends_count <= @max_direct)
-- ORDER BY u.full_name;

-- [ADMIN 8] Activity in a date range.
-- Definition used here:
--   app_open_count = number of sessions started in the range;
--   people_chatted_with = distinct direct-chat partners with messages in range;
--   groups_chatted_in = distinct group conversations with messages in range;
--   messages_sent = messages sent by the user in the range;
--   activity_count = app_open_count + messages_sent.
-- WITH app AS (
--   SELECT user_id, COUNT(*) AS app_open_count
--   FROM user_sessions
--   WHERE started_at >= @from_time AND started_at < @to_time
--   GROUP BY user_id
-- ), chat_stats AS (
--   SELECT cm.user_id,
--     COUNT(DISTINCT CASE WHEN c.conversation_type='DIRECT'
--       THEN CASE WHEN dc.user_low_id=cm.user_id THEN dc.user_high_id ELSE dc.user_low_id END
--       END) AS people_chatted_with,
--     COUNT(DISTINCT CASE WHEN c.conversation_type='GROUP' THEN c.conversation_id END)
--       AS groups_chatted_in
--   FROM messages m
--   JOIN conversations c ON c.conversation_id=m.conversation_id
--   JOIN conversation_members cm ON cm.conversation_id=m.conversation_id
--   LEFT JOIN direct_conversations dc ON dc.conversation_id=c.conversation_id
--   WHERE m.created_at >= @from_time AND m.created_at < @to_time
--     AND cm.joined_at <= m.created_at
--     AND (cm.left_at IS NULL OR cm.left_at >= m.created_at)
--   GROUP BY cm.user_id
-- ), sent AS (
--   SELECT sender_id AS user_id, COUNT(*) AS messages_sent
--   FROM messages
--   WHERE created_at >= @from_time AND created_at < @to_time
--   GROUP BY sender_id
-- )
-- SELECT u.user_id, u.username, u.full_name,
--        COALESCE(a.app_open_count,0) AS app_open_count,
--        COALESCE(cs.people_chatted_with,0) AS people_chatted_with,
--        COALESCE(cs.groups_chatted_in,0) AS groups_chatted_in,
--        COALESCE(s.messages_sent,0) AS messages_sent,
--        COALESCE(a.app_open_count,0)+COALESCE(s.messages_sent,0) AS activity_count
-- FROM users u
-- LEFT JOIN app a ON a.user_id=u.user_id
-- LEFT JOIN chat_stats cs ON cs.user_id=u.user_id
-- LEFT JOIN sent s ON s.user_id=u.user_id
-- WHERE u.status <> 'DELETED'
--   AND (COALESCE(a.app_open_count,0)>0 OR COALESCE(cs.people_chatted_with,0)>0
--        OR COALESCE(cs.groups_chatted_in,0)>0)
--   AND (@name IS NULL OR u.full_name LIKE CONCAT('%',@name,'%'))
-- ORDER BY activity_count DESC;

-- [ADMIN 9] Distinct users who opened the app by month in a selected year.
-- WITH RECURSIVE months AS (
--   SELECT 1 AS month_no UNION ALL SELECT month_no + 1 FROM months WHERE month_no < 12
-- ), active_counts AS (
--   SELECT MONTH(started_at) AS month_no, COUNT(DISTINCT user_id) AS total
--   FROM user_sessions
--   WHERE YEAR(started_at)=@selected_year
--   GROUP BY MONTH(started_at)
-- )
-- SELECT months.month_no, COALESCE(active_counts.total,0) AS active_users
-- FROM months LEFT JOIN active_counts USING(month_no)
-- ORDER BY months.month_no;

-- [USER 6/8] Friends with online/offline status (2-minute heartbeat threshold).
-- SELECT u.user_id, u.username, u.full_name,
--        EXISTS (
--          SELECT 1 FROM user_sessions s
--          WHERE s.user_id=u.user_id AND s.ended_at IS NULL
--            AND s.last_seen_at >= NOW(6) - INTERVAL 2 MINUTE
--        ) AS is_online
-- FROM friendships f
-- JOIN users u ON u.user_id = CASE WHEN f.user_low_id=@me THEN f.user_high_id ELSE f.user_low_id END
-- WHERE (f.user_low_id=@me OR f.user_high_id=@me)
--   AND u.status='ACTIVE'
-- ORDER BY u.full_name;

-- [USER 9] Search users, excluding blocks in either direction and the current user.
-- SELECT u.user_id, u.username, u.full_name
-- FROM users u
-- WHERE u.user_id <> @me AND u.status='ACTIVE'
--   AND (u.username LIKE CONCAT('%',@keyword,'%') OR u.full_name LIKE CONCAT('%',@keyword,'%'))
--   AND NOT EXISTS (SELECT 1 FROM user_blocks b
--                   WHERE (b.blocker_id=@me AND b.blocked_id=u.user_id)
--                      OR (b.blocker_id=u.user_id AND b.blocked_id=@me));

-- [USER 11f] Search messages in one direct conversation (plaintext messages only).
-- FULLTEXT can return message_id + created_at; the UI can open that exact message/thread.
-- SELECT m.message_id, m.conversation_id, m.sender_id, m.body_text, m.created_at
-- FROM direct_conversations dc
-- JOIN messages m ON m.conversation_id=dc.conversation_id
-- LEFT JOIN conversation_clear_markers ccm
--   ON ccm.conversation_id=m.conversation_id AND ccm.user_id=@me
-- LEFT JOIN message_hidden_for mh
--   ON mh.message_id=m.message_id AND mh.user_id=@me
-- WHERE ((dc.user_low_id=@me AND dc.user_high_id=@other_user)
--     OR (dc.user_low_id=@other_user AND dc.user_high_id=@me))
--   AND m.body_text IS NOT NULL AND m.deleted_at IS NULL
--   AND MATCH(m.body_text) AGAINST(@search_text IN BOOLEAN MODE)
--   AND (ccm.cleared_at IS NULL OR m.created_at > ccm.cleared_at)
--   AND mh.message_id IS NULL
-- ORDER BY m.created_at;
-- [USER 11g] Search all conversations belonging to the current user.
-- SELECT m.message_id, m.conversation_id, m.sender_id, m.body_text, m.created_at
-- FROM conversation_members cm
-- JOIN messages m ON m.conversation_id=cm.conversation_id
-- LEFT JOIN conversation_clear_markers ccm
--   ON ccm.conversation_id=m.conversation_id AND ccm.user_id=@me
-- LEFT JOIN message_hidden_for mh
--   ON mh.message_id=m.message_id AND mh.user_id=@me
-- WHERE cm.user_id=@me
--   AND m.body_text IS NOT NULL AND m.deleted_at IS NULL
--   AND MATCH(m.body_text) AGAINST(@search_text IN BOOLEAN MODE)
--   AND (ccm.cleared_at IS NULL OR m.created_at > ccm.cleared_at)
--   AND mh.message_id IS NULL
-- ORDER BY m.created_at;
-- NOTE: Server-side FULLTEXT search cannot search E2EE ciphertext. For encrypted groups,
-- download authorized ciphertext, decrypt on the client, then search locally; searchable
-- encryption would introduce additional security/design trade-offs.

-- [USER 11] Load visible messages for a conversation (supports clear-all and hide-selected).
-- SELECT m.*
-- FROM messages m
-- LEFT JOIN conversation_clear_markers ccm
--   ON ccm.conversation_id=m.conversation_id AND ccm.user_id=@me
-- LEFT JOIN message_hidden_for mh
--   ON mh.message_id=m.message_id AND mh.user_id=@me
-- WHERE m.conversation_id=@conversation_id
--   AND m.deleted_at IS NULL
--   AND (ccm.cleared_at IS NULL OR m.created_at > ccm.cleared_at)
--   AND mh.message_id IS NULL
-- ORDER BY m.created_at, m.message_id;

-- To clear one user's history:
-- INSERT INTO conversation_clear_markers(conversation_id,user_id,cleared_at)
-- VALUES(@conversation_id,@me,NOW(6))
-- ON DUPLICATE KEY UPDATE cleared_at=VALUES(cleared_at);
-- To hide selected message(s) for one user:
-- INSERT IGNORE INTO message_hidden_for(message_id,user_id) VALUES(@message_id,@me);

-- [USER 6c] Blocking a user: run these statements in one transaction.
-- START TRANSACTION;
-- INSERT IGNORE INTO user_blocks(blocker_id,blocked_id) VALUES(@me,@other_user);
-- DELETE FROM friendships
-- WHERE user_low_id=LEAST(@me,@other_user) AND user_high_id=GREATEST(@me,@other_user);
-- UPDATE friend_requests
-- SET status='CANCELLED', responded_at=NOW(6)
-- WHERE status='PENDING'
--   AND ((sender_id=@me AND receiver_id=@other_user)
--     OR (sender_id=@other_user AND receiver_id=@me));
-- COMMIT;

-- IMPORTANT APPLICATION RULES:
-- 0) On registration, insert the USER role in user_roles in the same transaction.
--    Grant ADMIN only through a protected admin-management flow/manual bootstrap.
-- 1) Check users.status='ACTIVE' before login, sending messages or creating relationships.
-- 2) A friend request must be rejected if either direction exists in user_blocks,
--    if a friendship already exists, or if a pending request already exists.
-- 3) On accepting a request, insert LEAST/GREATEST into friendships and set request ACCEPTED
--    in the same transaction.
-- 4) Creating a group: insert conversations + chat_groups + creator as ADMIN + at least
--    one other active member in one transaction.
-- 5) Only an active group ADMIN may add/remove members or promote another member to ADMIN.
-- 6) When group encryption is enabled, encrypt messages on the client; store only ciphertext.
--    When adding/removing a member, rotate the group key, increment current_key_version,
--    and insert new conversation_key_envelopes for remaining active members. A removed member
--    must not receive the new key. Old keys cannot be revoked from devices that already obtained them.
-- 7) Online state is approximate: update user_sessions.last_seen_at periodically and treat
--    a session as online only within a short heartbeat window (2 minutes in the sample query).
-- 8) Use PreparedStatement parameters; never concatenate user input into SQL strings.
