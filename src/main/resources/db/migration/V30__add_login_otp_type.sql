-- Add LOGIN to the otp_type check constraint
ALTER TABLE otp DROP CONSTRAINT IF EXISTS chk_otp_type;

ALTER TABLE otp ADD CONSTRAINT chk_otp_type
    CHECK (otp_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'EMAIL_CHANGE', 'MOBILE_CHANGE', 'LOGIN'));
