CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    transfer_id VARCHAR(36),
    account_id VARCHAR(32) NOT NULL,
    customer_id VARCHAR(32) NOT NULL,
    counterparty_account_id VARCHAR(32),
    transaction_type VARCHAR(24) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id VARCHAR(64) NOT NULL
);

CREATE INDEX idx_transactions_account_time ON transactions (account_id, occurred_at DESC);
CREATE INDEX idx_transactions_customer_time ON transactions (customer_id, occurred_at DESC);
CREATE INDEX idx_transactions_transfer ON transactions (transfer_id) WHERE transfer_id IS NOT NULL;
