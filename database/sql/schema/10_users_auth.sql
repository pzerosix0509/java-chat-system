CREATE TABLE IF NOT EXISTS roles (
    role_id SMALLINT GENERATED ALWAYS AS IDENTITY,
    role_code VARCHAR(30) NOT NULL,
    role_name VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_code UNIQUE (role_code)
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(50) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(20) NOT NULL DEFAULT 'PREFER_NOT_TO_SAY',
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at TIMESTAMPTZ(6),
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_gender CHECK (gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','LOCKED','DELETED')),
    CONSTRAINT chk_users_deleted_at CHECK (
      (status = 'DELETED' AND deleted_at IS NOT NULL)
      OR (status <> 'DELETED' AND deleted_at IS NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(full_name);
CREATE INDEX IF NOT EXISTS idx_users_status_created ON users(status, created_at);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id SMALLINT NOT NULL,
    assigned_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    assigned_by BIGINT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_user_role_assigner FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id, user_id);

INSERT INTO roles(role_code, role_name) VALUES
  ('USER', 'Người sử dụng'), ('ADMIN', 'Quản trị viên')
ON CONFLICT (role_code) DO NOTHING;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    reset_id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMPTZ(6) NOT NULL,
    used_at TIMESTAMPTZ(6),
    PRIMARY KEY (reset_id),
    CONSTRAINT uq_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_reset_expiry CHECK (expires_at > created_at)
);

CREATE INDEX IF NOT EXISTS idx_reset_user_expiry ON password_reset_tokens(user_id, expires_at, used_at);

CREATE TABLE IF NOT EXISTS login_history (
    login_id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    username_snapshot VARCHAR(50) NOT NULL,
    full_name_snapshot VARCHAR(150) NOT NULL,
    login_status VARCHAR(10) NOT NULL,
    occurred_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    PRIMARY KEY (login_id),
    CONSTRAINT chk_login_status CHECK (login_status IN ('SUCCESS','FAILED','LOCKED')),
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_login_time ON login_history(occurred_at);
CREATE INDEX IF NOT EXISTS idx_login_user_time ON login_history(user_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_login_username_time ON login_history(username_snapshot, occurred_at);

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    started_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_seen_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ended_at TIMESTAMPTZ(6),
    ip_address VARCHAR(45),
    device_info VARCHAR(500),
    PRIMARY KEY (session_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_session_times CHECK (last_seen_at >= started_at AND (ended_at IS NULL OR ended_at >= started_at))
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_started ON user_sessions(user_id, started_at);
CREATE INDEX IF NOT EXISTS idx_sessions_presence ON user_sessions(user_id, ended_at, last_seen_at);
CREATE INDEX IF NOT EXISTS idx_sessions_started ON user_sessions(started_at);

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
