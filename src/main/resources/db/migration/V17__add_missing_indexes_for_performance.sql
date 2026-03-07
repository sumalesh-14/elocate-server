-- Migration V17: Add missing indexes for query performance optimization

-- Recycle Request indexes
CREATE INDEX IF NOT EXISTS idx_recycle_request_user_status 
ON recycle_request(user_id, status);

CREATE INDEX IF NOT EXISTS idx_recycle_request_facility_status 
ON recycle_request(recycling_facility_id, status) 
WHERE recycling_facility_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_recycle_request_driver 
ON recycle_request(assigned_driver_id) 
WHERE assigned_driver_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_recycle_request_fulfillment_status 
ON recycle_request(fulfillment_status);

CREATE INDEX IF NOT EXISTS idx_recycle_request_created_at 
ON recycle_request(created_at DESC);

-- User indexes
CREATE INDEX IF NOT EXISTS idx_user_email_active 
ON "user"(email, is_active);

CREATE INDEX IF NOT EXISTS idx_user_is_active 
ON "user"(is_active);

-- Recycling Facility indexes
CREATE INDEX IF NOT EXISTS idx_recycling_facility_verified_active 
ON recycling_facility(is_verified, is_active);

CREATE INDEX IF NOT EXISTS idx_recycling_facility_email 
ON recycling_facility(email) 
WHERE email IS NOT NULL;

-- Driver indexes
CREATE INDEX IF NOT EXISTS idx_driver_availability 
ON driver(availability);

CREATE INDEX IF NOT EXISTS idx_driver_email 
ON driver(email);

-- Wallet Transaction indexes
CREATE INDEX IF NOT EXISTS idx_wallet_transaction_user_created 
ON wallet_transaction(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_wallet_transaction_type 
ON wallet_transaction(transaction_type);

-- Admin Audit Log indexes
CREATE INDEX IF NOT EXISTS idx_admin_audit_log_entity 
ON admin_audit_log(entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_admin_audit_log_action_created 
ON admin_audit_log(action_type, created_at DESC);

-- Recycle Status History indexes
CREATE INDEX IF NOT EXISTS idx_recycle_status_history_request_created 
ON recycle_status_history(recycle_request_id, created_at DESC);

-- Metal Rate indexes
CREATE INDEX IF NOT EXISTS idx_metal_rate_type_active 
ON metal_rate(metal_type, is_active);

CREATE INDEX IF NOT EXISTS idx_metal_rate_effective_dates 
ON metal_rate(effective_from, effective_to) 
WHERE is_active = true;

-- Device Model indexes
CREATE INDEX IF NOT EXISTS idx_device_model_category_brand 
ON device_model(category_id, brand_id);

CREATE INDEX IF NOT EXISTS idx_device_model_active 
ON device_model(is_active);

-- Add comments
COMMENT ON INDEX idx_recycle_request_user_status IS 'Optimize queries for user requests by status';
COMMENT ON INDEX idx_recycle_request_facility_status IS 'Optimize queries for facility requests by status';
COMMENT ON INDEX idx_wallet_transaction_user_created IS 'Optimize wallet transaction history queries';
