-- Migration V12: Add currency and conversion rate fields to user_wallet table

ALTER TABLE user_wallet
ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) DEFAULT 'USD',
ADD COLUMN IF NOT EXISTS points_to_money_rate DECIMAL(10,4) DEFAULT 0.01;

-- Add check constraint for valid currency codes (ISO 4217)
ALTER TABLE user_wallet
ADD CONSTRAINT chk_user_wallet_currency_code 
CHECK (currency_code ~ '^[A-Z]{3}$');

-- Add check constraint for positive conversion rate
ALTER TABLE user_wallet
ADD CONSTRAINT chk_user_wallet_conversion_rate 
CHECK (points_to_money_rate > 0);

-- Add index for querying by currency
CREATE INDEX IF NOT EXISTS idx_user_wallet_currency 
ON user_wallet(currency_code);

-- Update existing records to have default values
UPDATE user_wallet 
SET currency_code = 'USD', 
    points_to_money_rate = 0.01 
WHERE currency_code IS NULL OR points_to_money_rate IS NULL;

-- Add comments
COMMENT ON COLUMN user_wallet.currency_code IS 'ISO 4217 currency code (e.g., USD, EUR, INR)';
COMMENT ON COLUMN user_wallet.points_to_money_rate IS 'Conversion rate from points to monetary amount';
