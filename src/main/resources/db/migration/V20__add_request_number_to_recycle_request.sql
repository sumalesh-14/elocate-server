-- Add request_number field for human-readable request IDs
ALTER TABLE recycle_request
ADD COLUMN request_number VARCHAR(50) UNIQUE;

-- Create index for faster lookups
CREATE INDEX idx_recycle_request_number ON recycle_request(request_number);

-- Create sequence for generating sequential numbers
CREATE SEQUENCE IF NOT EXISTS recycle_request_seq START WITH 1 INCREMENT BY 1;

-- Add comment
COMMENT ON COLUMN recycle_request.request_number IS 'Human-readable request ID (e.g., RCY-2024-001234)';
