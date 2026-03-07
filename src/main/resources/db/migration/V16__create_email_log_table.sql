-- Migration V16: Create email_log table for tracking all email notifications

CREATE TABLE IF NOT EXISTS email_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_email VARCHAR(255) NOT NULL,
    recipient_user_id UUID,
    email_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100),
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_email_log_recipient_user 
        FOREIGN KEY (recipient_user_id) REFERENCES "user"(id) ON DELETE SET NULL,
    CONSTRAINT chk_email_log_status 
        CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- Add indexes for efficient querying
CREATE INDEX idx_email_log_recipient 
ON email_log(recipient_email);

CREATE INDEX idx_email_log_user 
ON email_log(recipient_user_id);

CREATE INDEX idx_email_log_status 
ON email_log(status);

CREATE INDEX idx_email_log_email_type 
ON email_log(email_type);

CREATE INDEX idx_email_log_created_at 
ON email_log(created_at DESC);

CREATE INDEX idx_email_log_related_entity 
ON email_log(related_entity_type, related_entity_id);

-- Add comments
COMMENT ON TABLE email_log IS 'Audit trail for all email notifications sent by the system';
COMMENT ON COLUMN email_log.recipient_email IS 'Email address of recipient';
COMMENT ON COLUMN email_log.recipient_user_id IS 'User ID if recipient is a registered user';
COMMENT ON COLUMN email_log.email_type IS 'Type of email (e.g., REQUEST_CREATED, PICKUP_SCHEDULED)';
COMMENT ON COLUMN email_log.subject IS 'Email subject line';
COMMENT ON COLUMN email_log.template_name IS 'Name of email template used';
COMMENT ON COLUMN email_log.related_entity_type IS 'Type of related entity (e.g., RECYCLE_REQUEST, FACILITY)';
COMMENT ON COLUMN email_log.related_entity_id IS 'ID of related entity';
COMMENT ON COLUMN email_log.status IS 'Email sending status (PENDING, SENT, FAILED)';
COMMENT ON COLUMN email_log.error_message IS 'Error message if sending failed';
COMMENT ON COLUMN email_log.sent_at IS 'Timestamp when email was successfully sent';
