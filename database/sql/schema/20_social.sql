CREATE TABLE IF NOT EXISTS friendships (
    user_low_id BIGINT NOT NULL,
    user_high_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_low_id, user_high_id),
    CONSTRAINT fk_friend_low FOREIGN KEY (user_low_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_friend_high FOREIGN KEY (user_high_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_friend_order CHECK (user_low_id < user_high_id)
);

CREATE INDEX IF NOT EXISTS idx_friendships_high ON friendships(user_high_id);

CREATE TABLE IF NOT EXISTS friend_requests (
    request_id BIGINT GENERATED ALWAYS AS IDENTITY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    responded_at TIMESTAMPTZ(6),
    pending_low_id BIGINT GENERATED ALWAYS AS
      (CASE WHEN status = 'PENDING' THEN LEAST(sender_id, receiver_id) ELSE NULL END) STORED,
    pending_high_id BIGINT GENERATED ALWAYS AS
      (CASE WHEN status = 'PENDING' THEN GREATEST(sender_id, receiver_id) ELSE NULL END) STORED,
    PRIMARY KEY (request_id),
    CONSTRAINT uq_one_pending_request_per_pair UNIQUE (pending_low_id, pending_high_id),
    CONSTRAINT chk_friend_request_status CHECK (status IN ('PENDING','ACCEPTED','REJECTED','CANCELLED')),
    CONSTRAINT chk_friend_req_not_self CHECK (sender_id <> receiver_id),
    CONSTRAINT fk_friend_req_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_friend_req_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_friend_req_receiver ON friend_requests(receiver_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_friend_req_sender ON friend_requests(sender_id, status, created_at);

CREATE TABLE IF NOT EXISTS user_blocks (
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT fk_block_blocker FOREIGN KEY (blocker_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_block_blocked FOREIGN KEY (blocked_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_block_not_self CHECK (blocker_id <> blocked_id)
);

CREATE INDEX IF NOT EXISTS idx_blocks_blocked ON user_blocks(blocked_id, blocker_id);
