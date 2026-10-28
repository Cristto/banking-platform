-- Add refund_transaction_id to purchases for UC04.5 (Refund Purchase).
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS refund_transaction_id BIGINT REFERENCES transactions (id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_purchases_refund ON purchases (refund_transaction_id);
