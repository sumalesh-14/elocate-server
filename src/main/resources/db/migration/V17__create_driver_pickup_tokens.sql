-- Create driver pickup tokens table for secure email links
CREATE TABLE IF NOT EXISTS driver_pickup_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recycle_request_id UUID NOT NULL REFERENCES recycle_requests(id) ON DELETE CASCADE,
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    action_type VARCHAR(20) NOT NULL CHECK (action_type IN ('ACCEPT', 'REJECT')),
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_driver_pickup_tokens_token ON driver_pickup_tokens(token);
CREATE INDEX idx_driver_pickup_tokens_request ON driver_pickup_tokens(recycle_request_id);
CREATE INDEX idx_driver_pickup_tokens_driver ON driver_pickup_tokens(driver_id);
CREATE INDEX idx_driver_pickup_tokens_expires ON driver_pickup_tokens(expires_at);

-- Add columns to recycle_requests for pickup photos and driver comments
ALTER TABLE recycle_requests 
ADD COLUMN IF NOT EXISTS pickup_photo_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS driver_comments TEXT;

-- Add comment
COMMENT ON TABLE driver_pickup_tokens IS 'Secure tokens for driver pickup email links with expiry';
COMMENT ON COLUMN recycle_requests.pickup_photo_url IS 'S3 URL of photo taken during pickup';
COMMENT ON COLUMN recycle_requests.driver_comments IS 'Comments from driver during pickup';
