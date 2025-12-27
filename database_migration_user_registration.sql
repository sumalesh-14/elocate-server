-- ==============================================================
-- User Registration and Profile System Database Migration
-- ==============================================================
-- This script creates the User table and updates UserAddress table

-- Step 1: Create User table
CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 2: Create index on mobile number for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_mobile_number ON "user"(mobile_number);

-- Step 3: Update UserAddress table to add city, state, pincode fields
ALTER TABLE user_address
ADD COLUMN IF NOT EXISTS city VARCHAR(100),
ADD COLUMN IF NOT EXISTS state VARCHAR(100),
ADD COLUMN IF NOT EXISTS pincode VARCHAR(6);

-- ==============================================================
-- Verification Queries
-- ==============================================================

-- Check User table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'user'
ORDER BY ordinal_position;

-- Check UserAddress table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'user_address'
ORDER BY ordinal_position;

-- Check if user_wallet table exists (should already exist)
SELECT table_name 
FROM information_schema.tables 
WHERE table_name = 'user_wallet';
