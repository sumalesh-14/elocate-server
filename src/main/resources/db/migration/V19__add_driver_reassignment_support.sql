-- Add isActive field to driver_pickup_tokens for token invalidation on reassignment
ALTER TABLE driver_pickup_tokens
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Add index for faster queries on active tokens
CREATE INDEX idx_driver_pickup_tokens_active ON driver_pickup_tokens(is_active);

-- Add index for SLA breach detection (assigned_at + driver_id)
CREATE INDEX idx_recycle_request_driver_assigned ON recycle_request(assigned_driver_id, updated_at)
WHERE fulfillment_status = 'DRIVER_ASSIGNED';

-- Add comment
COMMENT ON COLUMN driver_pickup_tokens.is_active IS 'Token becomes inactive when driver is reassigned';
