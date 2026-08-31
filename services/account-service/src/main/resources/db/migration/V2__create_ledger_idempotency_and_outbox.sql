CREATE TABLE ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    transfer_id VARCHAR(36),
    account_id VARCHAR(32) NOT NULL REFERENCES accounts(id),
    customer_id VARCHAR(32) NOT NULL,
    entry_type VARCHAR(24) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ledger_account_time ON ledger_entries (account_id, occurred_at DESC);
CREATE INDEX idx_ledger_transfer ON ledger_entries (transfer_id) WHERE transfer_id IS NOT NULL;

CREATE TABLE idempotency_records (
    id VARCHAR(36) PRIMARY KEY,
    actor_id VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    operation_type VARCHAR(24) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_idempotency_actor_key UNIQUE (actor_id, idempotency_key)
);

CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
