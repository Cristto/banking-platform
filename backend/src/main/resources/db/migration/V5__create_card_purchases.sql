CREATE TABLE card_purchases (
    id                      BIGSERIAL PRIMARY KEY,
    card_id                 BIGINT       NOT NULL REFERENCES cards (id),
    purchase_reference      VARCHAR(80)  NOT NULL UNIQUE,
    merchant_name           VARCHAR(150) NOT NULL,
    amount                  NUMERIC(18,2) NOT NULL,
    status                  VARCHAR(20)  NOT NULL,
    refunded_amount         NUMERIC(18,2) NOT NULL DEFAULT 0.00,
    settlement_transaction_id BIGINT UNIQUE REFERENCES transactions (id),
    authorized_at           TIMESTAMPTZ,
    settled_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_purchases_card ON card_purchases (card_id);
CREATE INDEX idx_card_purchases_settlement ON card_purchases (settlement_transaction_id);
