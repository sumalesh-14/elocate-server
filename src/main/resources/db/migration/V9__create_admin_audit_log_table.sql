CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL,
    admin_name VARCHAR(255),
    action_type VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
