-- Create the purchases table directly with account_id (the final structure).
-- This migration works both from a fresh database and when upgrading from V8.

CREATE TABLE IF NOT EXISTS purchases (
    id                      BIGSERIAL PRIMARY KEY,
    account_id              BIGINT       NOT NULL REFERENCES accounts (id),
    purchase_reference      VARCHAR(80)  NOT NULL UNIQUE,
    merchant_name           VARCHAR(150) NOT NULL,
    amount                  NUMERIC(18,2) NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    refunded_amount         NUMERIC(18,2) NOT NULL DEFAULT 0.00,
    settlement_transaction_id BIGINT UNIQUE REFERENCES transactions (id),
    authorized_at           TIMESTAMPTZ,
    settled_at              TIMESTAMPTZ,
    rejected_at             TIMESTAMP,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_purchases_account ON purchases (account_id);
CREATE INDEX IF NOT EXISTS idx_purchases_settlement ON purchases (settlement_transaction_id);

-- Migrate data from card_purchases to purchases (if upgrading from V8).
-- When running from scratch, card_purchases won't exist and this will be skipped.
INSERT INTO purchases (id, account_id, purchase_reference, merchant_name, amount, status,
                      refunded_amount, settlement_transaction_id, authorized_at, settled_at, created_at)
SELECT cp.id, c.account_id, cp.purchase_reference, cp.merchant_name, cp.amount, cp.status,
       cp.refunded_amount, cp.settlement_transaction_id, cp.authorized_at, cp.settled_at, cp.created_at
FROM card_purchases cp
JOIN cards c ON cp.card_id = c.id
ON CONFLICT (purchase_reference) DO NOTHING;

-- Drop old tables and indexes (safe when they don't exist).
DROP INDEX IF EXISTS idx_card_purchases_card;
DROP INDEX IF EXISTS idx_card_purchases_settlement;
DROP INDEX IF EXISTS idx_cards_account;
DROP INDEX IF EXISTS idx_cards_customer;
DROP TABLE IF EXISTS card_purchases;
DROP TABLE IF EXISTS cards;
