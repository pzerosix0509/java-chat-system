-- One-time migration for databases created before the LLM context fields existed.
-- Run this file only against an existing chat_app_db that already has llm_usage_logs.
USE chat_app_db;

ALTER TABLE llm_usage_logs
    ADD COLUMN conversation_id BIGINT UNSIGNED NULL AFTER user_id,
    ADD COLUMN source_message_id BIGINT UNSIGNED NULL AFTER conversation_id,
    ADD COLUMN error_message VARCHAR(500) NULL AFTER status,
    ADD KEY idx_llm_conversation (conversation_id, created_at),
    ADD KEY idx_llm_source_message (source_message_id),
    ADD CONSTRAINT fk_llm_conversation FOREIGN KEY (conversation_id)
      REFERENCES conversations(conversation_id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT fk_llm_source_message FOREIGN KEY (source_message_id)
      REFERENCES messages(message_id) ON DELETE SET NULL ON UPDATE CASCADE;
