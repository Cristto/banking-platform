-- Drop indexes on card_purchases before renaming
DROP INDEX IF EXISTS idx_card_purchases_card;
DROP INDEX IF EXISTS idx_card_purchases_settlement;

-- Rename card_purchases table to purchases
ALTER TABLE card_purchases RENAME TO purchases;

-- Rename card_id column to account_id
ALTER TABLE purchases RENAME COLUMN card_id TO account_id;

-- Update foreign key constraint name and target
ALTER TABLE purchases DROP CONSTRAINT IF EXISTS card_purchases_card_id_fkey;
ALTER TABLE purchases ADD CONSTRAINT purchases_account_id_fk FOREIGN KEY (account_id) REFERENCES accounts (id);

-- Drop indexes on cards before dropping the table
DROP INDEX IF EXISTS idx_cards_account;
DROP INDEX IF EXISTS idx_cards_customer;

-- Drop cards table (no longer needed; purchases are now linked directly to accounts)
DROP TABLE cards;
