USE chat_app_db;

CREATE TABLE IF NOT EXISTS friendships (
    user_low_id BIGINT UNSIGNED NOT NULL,
    user_high_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_low_id, user_high_id),
    KEY idx_friendships_high (user_high_id),
    CONSTRAINT fk_friend_low FOREIGN KEY (user_low_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_friend_high FOREIGN KEY (user_high_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS friend_requests (
    request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sender_id BIGINT UNSIGNED NOT NULL,
    receiver_id BIGINT UNSIGNED NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    responded_at DATETIME(6) NULL,
    pending_low_id BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN status = 'PENDING' THEN LEAST(sender_id, receiver_id) ELSE NULL END) STORED,
    pending_high_id BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN status = 'PENDING' THEN GREATEST(sender_id, receiver_id) ELSE NULL END) STORED,
    PRIMARY KEY (request_id),
    UNIQUE KEY uq_one_pending_request_per_pair (pending_low_id, pending_high_id),
    KEY idx_friend_req_receiver (receiver_id, status, created_at),
    KEY idx_friend_req_sender (sender_id, status, created_at),
    CONSTRAINT fk_friend_req_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_friend_req_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_friend_req_not_self CHECK (sender_id <> receiver_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_blocks (
    blocker_id BIGINT UNSIGNED NOT NULL,
    blocked_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (blocker_id, blocked_id),
    KEY idx_blocks_blocked (blocked_id, blocker_id),
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_block_not_self CHECK (blocker_id <> blocked_id)
) ENGINE=InnoDB;
