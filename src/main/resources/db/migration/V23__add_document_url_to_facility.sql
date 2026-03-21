-- Add document_url column to recycling_facility for partner verification documents
ALTER TABLE recycling_facility
    ADD COLUMN IF NOT EXISTS document_url TEXT;
