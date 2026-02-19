mi-- Add registration_number column to recycling_facility table
ALTER TABLE recycling_facility 
ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100) UNIQUE;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_facility_registration_number 
ON recycling_facility(registration_number);

-- Add comment
COMMENT ON COLUMN recycling_facility.registration_number IS 'Unique registration number for recycling facility partners';
