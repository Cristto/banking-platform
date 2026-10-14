CREATE TABLE transactions (
    id                    BIGSERIAL PRIMARY KEY,
    reference             VARCHAR(80)  NOT NULL UNIQUE,
    type                  VARCHAR(30)  NOT NULL,
    source_account_id     BIGINT REFERENCES accounts (id),
    destination_account_id BIGINT REFERENCES accounts (id),
    amount                NUMERIC(18,2) NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    idempotency_key       VARCHAR(100) UNIQUE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    description           VARCHAR(255)
);

CREATE INDEX idx_transactions_source ON transactions (source_account_id);
CREATE INDEX idx_transactions_destination ON transactions (destination_account_id);
