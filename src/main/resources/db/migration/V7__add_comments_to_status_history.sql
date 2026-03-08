-- Add comments column to recycle_status_history table
ALTER TABLE recycle_status_history 
ADD COLUMN comments VARCHAR(1000);

-- Add comment to describe the column
COMMENT ON COLUMN recycle_status_history.comments IS 'Optional comments or notes about the status change';
