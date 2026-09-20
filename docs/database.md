# Database ownership and schema

Nexa Bank uses database-per-service boundaries on one local PostgreSQL/pgvector instance. Services never read another service's tables. Cross-service identity and ownership checks use authenticated HTTP APIs, and state propagation uses Kafka events.

| Database | Owner | Primary data |
| --- | --- | --- |
| `nexa_customer` | Customer Service | Customer profiles and password credentials |
| `nexa_account` | Account Service | Accounts, immutable ledger entries, idempotency records, and transactional outbox events |
| `nexa_transaction` | Transaction Service | Idempotent transaction read projections built from Kafka events |
| `nexa_audit` | Audit Service | Security and banking audit records |
| `nexa_rag` | Agent Service | Spring AI `policy_vector_store` embeddings and policy metadata |

## Customer schema

`customers` stores the profile, lifecycle status, timestamps, and optimistic-lock version. Email uniqueness is enforced in PostgreSQL. `customer_credentials` stores only the password hash and role; plaintext passwords are never persisted.

```text
customers 1 ─── 1 customer_credentials
```

## Account and transfer schema

`accounts` is the balance authority. Monetary columns use `NUMERIC(19,2)` and enforce non-negative balances. A customer can hold at most one account of each type through `UNIQUE(customer_id, account_type)`.

Every deposit or transfer writes an immutable `ledger_entries` record. A transfer locks both account rows in stable identifier order, updates both balances, writes debit and credit ledger entries, persists the replayable result in `idempotency_records`, and inserts an `outbox_events` row in one database transaction.

```text
accounts 1 ─── * ledger_entries
       │
       ├──── idempotency_records  (actor + key is unique)
       └──── outbox_events        (unpublished rows have published_at = null)
```

The scheduled outbox publisher sends committed events to Kafka and sets `published_at` only after broker acknowledgement. This avoids the unsafe database-commit/Kafka-publish gap.

## Transaction projection

`transactions` is query-optimized history, not the balance authority. The event transaction ID is its primary key, so Kafka redelivery is naturally idempotent. Indexes support account/customer history in descending time order and transfer lookup. Account and customer identifiers deliberately have no cross-database foreign key.

## Audit schema

`audit_records` stores the event ID, authenticated actor, action, resource, status, timestamp, and request/correlation ID. It excludes passwords, JWTs, prompt bodies, and full account details.

## Policy vector store

The Agent Service enables Spring AI pgvector schema initialization for `policy_vector_store`, using an HNSW index and cosine distance. Ingested documents carry `bank_policy` metadata so policy answers can require retrieved evidence. Application migrations do not manage this framework-owned table.

## Redis and Kafka

Redis is intentionally not a system of record. It holds short-lived account metadata cache entries, five-minute transfer confirmations, and 30-minute customer-scoped conversation memory. Durable idempotency remains in PostgreSQL.

Kafka topics carry completed transfer and transaction events to the transaction projection, notification consumer, and audit consumer. Consumers use stable event or transaction identifiers so retries do not create duplicate records.

## Migration and initialization

PostgreSQL creates the five local databases from `infrastructure/docker/postgres/init-databases.sql` on the first empty-volume startup. Flyway migrations live with each owning service under `src/main/resources/db/migration`. Existing PostgreSQL volumes do not rerun the initialization script; use a new development volume when testing first-start database creation.

