# Nexa Bank deterministic backend architecture

```text
Client -> API Gateway (8080)
             |-- Customer Service (8081) -> nexa_customer
             |-- Account Service (8082)  -> nexa_account + Redis
             |-- Transaction Service (8083) -> nexa_transaction
                                              ^
Account outbox -> Kafka ----------------------|-- Notification Service -> structured logs
                                              `-- Audit Service -> nexa_audit
```

Account Service owns balances and performs source debit, destination credit, two immutable ledger inserts, idempotency persistence, and outbox insertion in one PostgreSQL transaction. Accounts are locked in stable ID order to avoid deadlocks. A scheduled publisher sends committed outbox events to Kafka and marks them published only after broker acknowledgement.

Transaction Service is a read projection, not the balance authority. Its consumers use transaction/event IDs as primary keys, making Kafka redelivery idempotent. Notification Service initially writes structured logs. Audit Service stores actor, action, resource, status, time, and correlation ID without tokens, passwords, or other secrets.

The gateway contains no banking rules. It validates JWT issuer/signature, permits public auth endpoints, routes downstream requests, preserves bearer tokens, and propagates `X-Correlation-ID`.

## Remaining phases

MCP tools, AI agent workflows, RAG/pgvector, frontend, service container images, observability, and Kubernetes are intentionally deferred until this backend passes live end-to-end verification.
