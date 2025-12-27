-- ============================================
-- OTP Authentication System Database Migration
-- ============================================

-- Step 1: Update User table
ALTER TABLE "user"
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(255);

-- Make password_hash NOT NULL after adding it (for new users)
-- Existing users would need a data migration strategy

-- Step 2: Create OTP table
CREATE TABLE IF NOT EXISTS otp (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    otp_type VARCHAR(50) NOT NULL,  -- EMAIL_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_otp_email_type ON otp(email, otp_type, is_used);
CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_email_verified ON "user"(is_email_verified);

-- Step 4: Add constraint to ensure OTP type is valid
ALTER TABLE otp
ADD CONSTRAINT chk_otp_type 
CHECK (otp_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'EMAIL_CHANGE'));

-- ============================================
-- Verification Queries
-- ============================================

-- Check User table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'user'
ORDER BY ordinal_position;

-- Check OTP table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'otp'
ORDER BY ordinal_position;

-- Check indexes
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE tablename IN ('user', 'otp');
