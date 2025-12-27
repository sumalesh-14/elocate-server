-- ============================================
-- Fulfillment System Database Migration
-- ============================================
-- This script adds pickup and drop-off fulfillment tracking to the recycle_request table

-- Step 1: Add fulfillment columns to recycle_request table
ALTER TABLE recycle_request
ADD COLUMN fulfillment_type VARCHAR(20),
ADD COLUMN fulfillment_status VARCHAR(50);

-- Step 2: Set default values for existing records (optional - adjust as needed)
-- This assumes all existing requests should be treated as pending pickup
UPDATE recycle_request
SET fulfillment_type = 'PICKUP',
    fulfillment_status = 'PICKUP_REQUESTED'
WHERE fulfillment_type IS NULL;

-- Step 3: Make columns NOT NULL after setting defaults
ALTER TABLE recycle_request
ALTER COLUMN fulfillment_type SET NOT NULL,
ALTER COLUMN fulfillment_status SET NOT NULL;

-- Step 4: Drop deprecated column (optional - only if you want to clean up)
-- ALTER TABLE recycle_request DROP COLUMN IF EXISTS is_pickup_require;

-- ============================================
-- Verification Queries
-- ============================================
-- Verify the changes
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'recycle_request'
  AND column_name IN ('fulfillment_type', 'fulfillment_status', 'pickup_address_id', 'recycling_facility_id');

-- Check sample data
SELECT id, fulfillment_type, fulfillment_status, status, pickup_address_id, recycling_facility_id
FROM recycle_request
LIMIT 5;
