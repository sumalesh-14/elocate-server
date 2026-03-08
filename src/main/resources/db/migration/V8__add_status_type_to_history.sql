-- Add status_type column to distinguish between RecycleStatus and FulfillmentStatus changes
ALTER TABLE recycle_status_history 
ADD COLUMN status_type VARCHAR(50);

-- Set default value for existing records
UPDATE recycle_status_history 
SET status_type = 'FULFILLMENT' 
WHERE status_type IS NULL;

-- Add index for better query performance
CREATE INDEX idx_status_history_type ON recycle_status_history(status_type);
