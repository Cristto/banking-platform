-- Add customer_id column to accounts
ALTER TABLE accounts ADD COLUMN customer_id BIGINT;
ALTER TABLE accounts ADD CONSTRAINT accounts_customer_id_fk FOREIGN KEY (customer_id) REFERENCES customers (id);

-- Populate customer_id from existing account_holders (take the first holder for each account)
UPDATE accounts SET customer_id = (
    SELECT ah.customer_id
    FROM account_holders ah
    WHERE ah.account_id = accounts.id
    LIMIT 1
);

-- Make customer_id NOT NULL
ALTER TABLE accounts ALTER COLUMN customer_id SET NOT NULL;

-- Drop indexes on account_holders
DROP INDEX IF EXISTS idx_account_holders_account;
DROP INDEX IF EXISTS idx_account_holders_customer;

-- Drop account_holders table
DROP TABLE account_holders;
