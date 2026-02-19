-- Fix address column length issue
-- Change address from varchar(255) to TEXT to support longer addresses

ALTER TABLE recycling_facility 
ALTER COLUMN address TYPE TEXT;
