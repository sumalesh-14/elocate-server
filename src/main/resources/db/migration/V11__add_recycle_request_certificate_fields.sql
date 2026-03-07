-- Migration V11: Add certificate and AI pricing fields to recycle_request table

ALTER TABLE recycle_request
ADD COLUMN IF NOT EXISTS certificate_image_url TEXT,
ADD COLUMN IF NOT EXISTS ai_pricing_response JSONB;

-- Add index for querying requests with certificates
CREATE INDEX IF NOT EXISTS idx_recycle_request_certificate 
ON recycle_request(certificate_image_url) 
WHERE certificate_image_url IS NOT NULL;

-- Add GIN index for JSONB ai_pricing_response for efficient querying
CREATE INDEX IF NOT EXISTS idx_recycle_request_ai_pricing 
ON recycle_request USING GIN (ai_pricing_response);

-- Add comments
COMMENT ON COLUMN recycle_request.certificate_image_url IS 'URL to generated recycling certificate image';
COMMENT ON COLUMN recycle_request.ai_pricing_response IS 'Cached AI pricing API response in JSON format';
