-- Add reserved_balance column to accounts (for purchase authorization).
-- Works both from scratch and when upgrading from V9.

ALTER TABLE accounts
ADD COLUMN IF NOT EXISTS reserved_balance NUMERIC(19, 2) NOT NULL DEFAULT 0;
