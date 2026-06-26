-- Tables are created/updated by Hibernate (ddl-auto=update).
-- This file adds indexes idempotently so they exist on both fresh and existing databases.
CREATE INDEX IF NOT EXISTS idx_follow_followee_id              ON user_follows(followee_id);
CREATE INDEX IF NOT EXISTS idx_follow_request_target_username  ON follow_requests(target_username);
CREATE INDEX IF NOT EXISTS idx_message_sender_recipient        ON messages(sender_username, recipient_username);
CREATE INDEX IF NOT EXISTS idx_message_recipient_username      ON messages(recipient_username);
CREATE INDEX IF NOT EXISTS idx_refresh_token_username          ON refresh_tokens(username);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_username ON notifications(recipient_username);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_seen     ON notifications(recipient_username, seen);
