-- Migration V14: Create pricing_history table for tracking price changes

CREATE TABLE IF NOT EXISTS pricing_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recycle_request_id UUID NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value DECIMAL(10,2),
    new_value DECIMAL(10,2) NOT NULL,
    changed_by UUID NOT NULL,
    change_reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pricing_history_request 
        FOREIGN KEY (recycle_request_id) REFERENCES recycle_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_pricing_history_changed_by 
        FOREIGN KEY (changed_by) REFERENCES "user"(id),
    CONSTRAINT chk_pricing_history_field_name 
        CHECK (field_name IN ('estimated_points', 'final_points'))
);

-- Add indexes for efficient querying
CREATE INDEX idx_pricing_history_request 
ON pricing_history(recycle_request_id);

CREATE INDEX idx_pricing_history_changed_by 
ON pricing_history(changed_by);

CREATE INDEX idx_pricing_history_created_at 
ON pricing_history(created_at DESC);

-- Add comments
COMMENT ON TABLE pricing_history IS 'Audit trail for all pricing changes on recycle requests';
COMMENT ON COLUMN pricing_history.field_name IS 'Which price field was changed (estimated_points or final_points)';
COMMENT ON COLUMN pricing_history.old_value IS 'Previous value before change';
COMMENT ON COLUMN pricing_history.new_value IS 'New value after change';
COMMENT ON COLUMN pricing_history.changed_by IS 'User who made the change (admin or intermediary)';
COMMENT ON COLUMN pricing_history.change_reason IS 'Reason for the price change';
