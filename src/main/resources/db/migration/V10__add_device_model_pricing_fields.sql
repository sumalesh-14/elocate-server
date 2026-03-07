-- Migration V10: Add pricing override fields to device_model table
-- These fields support manual price overrides by admins

ALTER TABLE device_model
ADD COLUMN IF NOT EXISTS ai_base_price DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS manual_price_override DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS price_override_reason TEXT,
ADD COLUMN IF NOT EXISTS price_override_by UUID,
ADD COLUMN IF NOT EXISTS price_override_at TIMESTAMP;

-- Add foreign key constraint for price_override_by
ALTER TABLE device_model
ADD CONSTRAINT fk_device_model_price_override_by 
FOREIGN KEY (price_override_by) REFERENCES "user"(id);

-- Add index for querying models with overrides
CREATE INDEX IF NOT EXISTS idx_device_model_price_override 
ON device_model(manual_price_override) 
WHERE manual_price_override IS NOT NULL;

-- Add comment
COMMENT ON COLUMN device_model.ai_base_price IS 'Base price from AI pricing API';
COMMENT ON COLUMN device_model.manual_price_override IS 'Manual price override set by admin';
COMMENT ON COLUMN device_model.price_override_reason IS 'Reason for manual price override';
COMMENT ON COLUMN device_model.price_override_by IS 'Admin user who set the override';
COMMENT ON COLUMN device_model.price_override_at IS 'Timestamp when override was set';
