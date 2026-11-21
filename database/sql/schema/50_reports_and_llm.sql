CREATE TABLE IF NOT EXISTS spam_reports (
    report_id BIGINT GENERATED ALWAYS AS IDENTITY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    message_id BIGINT,
    reason VARCHAR(120) NOT NULL DEFAULT 'SPAM',
    details TEXT,
    status VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reviewed_by BIGINT,
    reviewed_at TIMESTAMPTZ(6),
    PRIMARY KEY (report_id),
    CONSTRAINT chk_report_status CHECK (status IN ('OPEN','REVIEWED','DISMISSED','ACTIONED')),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_report_message FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE SET NULL ON UPDATE RESTRICT,
    CONSTRAINT fk_report_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE RESTRICT,
    CONSTRAINT chk_report_not_self CHECK (reporter_id <> reported_user_id),
    CONSTRAINT chk_report_review_time CHECK (reviewed_at IS NULL OR reviewed_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_reports_time ON spam_reports(created_at);
CREATE INDEX IF NOT EXISTS idx_reports_reported_user ON spam_reports(reported_user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reports_status_time ON spam_reports(status, created_at);
CREATE INDEX IF NOT EXISTS idx_reports_reporter ON spam_reports(reporter_id, created_at);

CREATE TABLE IF NOT EXISTS llm_usage_logs (
    usage_id BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    conversation_id BIGINT,
    source_message_id BIGINT,
    feature VARCHAR(20) NOT NULL,
    provider VARCHAR(80),
    model_name VARCHAR(120),
    input_tokens INTEGER,
    output_tokens INTEGER,
    status VARCHAR(10) NOT NULL,
    error_message VARCHAR(500),
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (usage_id),
    CONSTRAINT chk_llm_feature CHECK (feature IN ('REWRITE','SUGGEST_REPLY','OTHER')),
    CONSTRAINT chk_llm_status CHECK (status IN ('SUCCESS','FAILED')),
    CONSTRAINT fk_llm_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_llm_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE SET NULL ON UPDATE RESTRICT,
    CONSTRAINT fk_llm_source_message FOREIGN KEY (source_message_id) REFERENCES messages(message_id) ON DELETE SET NULL ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_llm_user_time ON llm_usage_logs(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_llm_conversation ON llm_usage_logs(conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_llm_source_message ON llm_usage_logs(source_message_id);
