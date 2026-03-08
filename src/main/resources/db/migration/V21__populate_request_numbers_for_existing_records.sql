-- Populate request_number for existing records that don't have one
-- This migration generates request numbers for older records

-- Function to generate request number based on created_at date and row number
DO $$
DECLARE
    rec RECORD;
    year_val INTEGER;
    seq_num INTEGER;
    new_request_number VARCHAR(50);
BEGIN
    -- Initialize counter
    seq_num := 0;
    
    -- Loop through all records without request_number, ordered by created_at
    FOR rec IN 
        SELECT id, EXTRACT(YEAR FROM created_at) as year_created
        FROM recycle_request 
        WHERE request_number IS NULL 
        ORDER BY created_at ASC
    LOOP
        -- Increment sequence number
        seq_num := seq_num + 1;
        year_val := rec.year_created;
        
        -- Generate request number: RCY-YYYY-NNNNNN
        new_request_number := 'RCY-' || year_val || '-' || LPAD(seq_num::TEXT, 6, '0');
        
        -- Update the record
        UPDATE recycle_request 
        SET request_number = new_request_number 
        WHERE id = rec.id;
        
        RAISE NOTICE 'Generated request number % for record %', new_request_number, rec.id;
    END LOOP;
    
    -- Update the sequence to start from the next number
    IF seq_num > 0 THEN
        PERFORM setval('recycle_request_seq', seq_num + 1, false);
        RAISE NOTICE 'Updated sequence to start from %', seq_num + 1;
    END IF;
END $$;

-- Verify all records now have request numbers
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count 
    FROM recycle_request 
    WHERE request_number IS NULL;
    
    IF null_count > 0 THEN
        RAISE EXCEPTION 'Migration failed: % records still have NULL request_number', null_count;
    ELSE
        RAISE NOTICE 'Migration successful: All records have request numbers';
    END IF;
END $$;
