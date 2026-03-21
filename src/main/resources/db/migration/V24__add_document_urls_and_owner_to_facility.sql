-- Replace single document_url with a document_urls array (TEXT[])

ALTER TABLE recycling_facility
    ADD COLUMN IF NOT EXISTS document_urls TEXT[];

-- Migrate existing document_url data into the new array column
UPDATE recycling_facility
SET document_urls = ARRAY[document_url]
WHERE document_url IS NOT NULL AND document_url <> '';
