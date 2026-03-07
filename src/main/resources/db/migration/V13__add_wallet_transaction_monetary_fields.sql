-- Migration V13: Add monetary amount and conversion rate fields to wallet_transaction table

ALTER TABLE wallet_transaction
ADD COLUMN IF NOT EXISTS conversion_rate DECIMAL(10,4),
ADD COLUMN IF NOT EXISTS monetary_amount DECIMAL(10,2);

-- Add check constraint for positive monetary amount
ALTER TABLE wallet_transaction
ADD CONSTRAINT chk_wallet_transaction_monetary_amount 
CHECK (monetary_amount IS NULL OR monetary_amount >= 0);

-- Add check constraint for positive conversion rate
ALTER TABLE wallet_transaction
ADD CONSTRAINT chk_wallet_transaction_conversion_rate 
CHECK (conversion_rate IS NULL OR conversion_rate > 0);

-- Add index for querying by monetary amount
CREATE INDEX IF NOT EXISTS idx_wallet_transaction_monetary_amount 
ON wallet_transaction(monetary_amount) 
WHERE monetary_amount IS NOT NULL;

-- Backfill existing transactions with conversion rate and monetary amount
-- Assuming default rate of 0.01 (1 point = $0.01)
UPDATE wallet_transaction 
SET conversion_rate = 0.01,
    monetary_amount = points * 0.01
WHERE conversion_rate IS NULL AND points IS NOT NULL;

-- Add comments
COMMENT ON COLUMN wallet_transaction.conversion_rate IS 'Points to money conversion rate at transaction time';
COMMENT ON COLUMN wallet_transaction.monetary_amount IS 'Monetary amount of transaction';
