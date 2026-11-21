USE chat_app_db;

CREATE TABLE IF NOT EXISTS spam_reports (
    report_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT UNSIGNED NOT NULL,
    reported_user_id BIGINT UNSIGNED NOT NULL,
    message_id BIGINT UNSIGNED NULL,
    reason VARCHAR(120) NOT NULL DEFAULT 'SPAM',
    details TEXT NULL,
    status ENUM('OPEN','REVIEWED','DISMISSED','ACTIONED') NOT NULL DEFAULT 'OPEN',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reviewed_by BIGINT UNSIGNED NULL,
    reviewed_at DATETIME(6) NULL,
    PRIMARY KEY (report_id),
    KEY idx_reports_time (created_at),
    KEY idx_reports_reported_user (reported_user_id, created_at),
    KEY idx_reports_status_time (status, created_at),
    KEY idx_reports_reporter (reporter_id, created_at),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_reported FOREIGN KEY (reported_user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_report_message FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_report_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_report_not_self CHECK (reporter_id <> reported_user_id),
    CONSTRAINT chk_report_review_time CHECK (reviewed_at IS NULL OR reviewed_at >= created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS llm_usage_logs (
    usage_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    feature ENUM('REWRITE','SUGGEST_REPLY','OTHER') NOT NULL,
    provider VARCHAR(80) NULL,
    model_name VARCHAR(120) NULL,
    input_tokens INT UNSIGNED NULL,
    output_tokens INT UNSIGNED NULL,
    status ENUM('SUCCESS','FAILED') NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (usage_id),
    KEY idx_llm_user_time (user_id, created_at),
    CONSTRAINT fk_llm_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
