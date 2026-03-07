-- Migration V15: Create facility_reassignment table for tracking facility changes

CREATE TABLE IF NOT EXISTS facility_reassignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recycle_request_id UUID NOT NULL,
    old_facility_id UUID,
    new_facility_id UUID NOT NULL,
    reassigned_by UUID NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_facility_reassignment_request 
        FOREIGN KEY (recycle_request_id) REFERENCES recycle_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_facility_reassignment_old_facility 
        FOREIGN KEY (old_facility_id) REFERENCES recycling_facility(id),
    CONSTRAINT fk_facility_reassignment_new_facility 
        FOREIGN KEY (new_facility_id) REFERENCES recycling_facility(id),
    CONSTRAINT fk_facility_reassignment_reassigned_by 
        FOREIGN KEY (reassigned_by) REFERENCES "user"(id)
);

-- Add indexes for efficient querying
CREATE INDEX idx_facility_reassignment_request 
ON facility_reassignment(recycle_request_id);

CREATE INDEX idx_facility_reassignment_new_facility 
ON facility_reassignment(new_facility_id);

CREATE INDEX idx_facility_reassignment_old_facility 
ON facility_reassignment(old_facility_id);

CREATE INDEX idx_facility_reassignment_created_at 
ON facility_reassignment(created_at DESC);

-- Add comments
COMMENT ON TABLE facility_reassignment IS 'Audit trail for facility reassignments by admin';
COMMENT ON COLUMN facility_reassignment.old_facility_id IS 'Previous facility (NULL if first assignment)';
COMMENT ON COLUMN facility_reassignment.new_facility_id IS 'New facility assigned to request';
COMMENT ON COLUMN facility_reassignment.reassigned_by IS 'Admin user who performed reassignment';
COMMENT ON COLUMN facility_reassignment.reason IS 'Reason for facility reassignment';
