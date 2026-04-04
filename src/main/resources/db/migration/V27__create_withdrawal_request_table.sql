-- V27: Create withdrawal_request table for citizen payout requests

CREATE TABLE withdrawal_request (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    amount          NUMERIC(10, 2) NOT NULL,

    -- Bank details
    account_holder_name VARCHAR(255) NOT NULL,
    mobile_number       VARCHAR(15)  NOT NULL,
    account_number      VARCHAR(18)  NOT NULL,
    bank_name           VARCHAR(255) NOT NULL,
    ifsc_code           VARCHAR(11)  NOT NULL,
    upi_id              VARCHAR(255),
    email               VARCHAR(255),

    -- Status
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    admin_note      TEXT,
    processed_by    UUID,
    requested_at    TIMESTAMP    NOT NULL DEFAULT now(),
    processed_at    TIMESTAMP
);

CREATE INDEX idx_withdrawal_user_id     ON withdrawal_request (user_id);
CREATE INDEX idx_withdrawal_status      ON withdrawal_request (status);
CREATE INDEX idx_withdrawal_requested_at ON withdrawal_request (requested_at DESC);
