-- Add certificate_url column to recycle_request table
ALTER TABLE recycle_request
ADD COLUMN certificate_url VARCHAR(500);

-- Add comment for documentation
COMMENT ON COLUMN recycle_request.certificate_url IS 'URL to the recycling certificate (generated after recycling is complete)';
