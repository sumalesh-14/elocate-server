-- Add new columns for driver tracking in pickup flow
ALTER TABLE recycle_request 
ADD COLUMN assigned_driver_id UUID,
ADD COLUMN driver_failure_reason VARCHAR(1000);
