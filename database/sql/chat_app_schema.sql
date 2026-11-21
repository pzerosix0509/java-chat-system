-- MySQL CLI entrypoint. Run from database/sql with:
--   SOURCE chat_app_schema.sql;
-- The numbered files are the authoritative schema split.
SOURCE schema/00_database.sql;
SOURCE schema/10_users_auth.sql;
SOURCE schema/20_social.sql;
SOURCE schema/30_conversations.sql;
SOURCE schema/40_messages.sql;
SOURCE schema/50_reports_and_llm.sql;
SOURCE schema/60_views.sql;
