CREATE TABLE accounts (
    id VARCHAR(32) PRIMARY KEY,
    customer_id VARCHAR(32) NOT NULL,
    account_number VARCHAR(16) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_account_type UNIQUE (customer_id, account_type)
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
