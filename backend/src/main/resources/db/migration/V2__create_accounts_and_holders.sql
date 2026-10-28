CREATE TABLE accounts (
    id             BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(34) NOT NULL UNIQUE,
    balance        NUMERIC(18,2) NOT NULL DEFAULT 0.00,
    reserved_balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency       VARCHAR(3)  NOT NULL DEFAULT 'EUR',
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at      TIMESTAMPTZ,
    version        BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE account_holders (
    id         BIGSERIAL PRIMARY KEY,
    account_id BIGINT       NOT NULL REFERENCES accounts (id),
    customer_id BIGINT      NOT NULL REFERENCES customers (id),
    role       VARCHAR(20)  NOT NULL,
    added_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, customer_id)
);

CREATE INDEX idx_account_holders_account ON account_holders (account_id);
CREATE INDEX idx_account_holders_customer ON account_holders (customer_id);
