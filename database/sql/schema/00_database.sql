-- PostgreSQL 16+ database bootstrap.
-- PostgreSQL has no CREATE DATABASE IF NOT EXISTS and CREATE DATABASE cannot
-- run inside a transaction. Create this database once from the postgres DB:
--   CREATE DATABASE chat_app_db;
-- Then connect IntelliJ/psql to chat_app_db before running the other files.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP(6);
    RETURN NEW;
END;
$$;
