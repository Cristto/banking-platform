-- Add rejected_at column to purchases for UC04.3 (Reject Settlement).
-- Works both from scratch and when upgrading from V11.

ALTER TABLE purchases
ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;
