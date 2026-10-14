CREATE TABLE cards (
    id               BIGSERIAL PRIMARY KEY,
    account_id       BIGINT       NOT NULL REFERENCES accounts (id),
    customer_id      BIGINT       NOT NULL REFERENCES customers (id),
    card_reference   VARCHAR(50)  NOT NULL UNIQUE,
    last_four        CHAR(4),
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    issued_at        TIMESTAMPTZ  NOT NULL,
    expires_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_cards_account ON cards (account_id);
CREATE INDEX idx_cards_customer ON cards (customer_id);
