-- Link users to recycling facilities for partner accounts
ALTER TABLE recycling_facility 
ADD COLUMN IF NOT EXISTS user_id UUID;

-- Add foreign key constraint
ALTER TABLE recycling_facility
ADD CONSTRAINT fk_facility_user 
FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE SET NULL;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_facility_user_id ON recycling_facility(user_id);

-- Add approval status column
ALTER TABLE recycling_facility
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'PENDING';

-- Add comment
COMMENT ON COLUMN recycling_facility.user_id IS 'User account associated with this facility (for partner login)';
COMMENT ON COLUMN recycling_facility.approval_status IS 'Approval status: PENDING, APPROVED, REJECTED';
