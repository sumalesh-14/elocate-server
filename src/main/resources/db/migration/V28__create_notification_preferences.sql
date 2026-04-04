CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id     UUID PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    new_requests    BOOLEAN NOT NULL DEFAULT TRUE,
    daily_summary   BOOLEAN NOT NULL DEFAULT TRUE,
    weekly_report   BOOLEAN NOT NULL DEFAULT FALSE,
    marketing       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);
