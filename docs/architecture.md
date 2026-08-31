# Nexa Bank deterministic backend architecture

```text
Client -> API Gateway (8080)
             |-- Customer Service (8081) -> nexa_customer
             |-- Account Service (8082)  -> nexa_account + Redis
             |-- Transaction Service (8083) -> nexa_transaction
             |-- Agent Service (8091) -> Ollama + per-request authenticated MCP client
             `-- Banking MCP Server (8090) -> authenticated REST calls back through Gateway
                                              ^
Account outbox -> Kafka ----------------------|-- Notification Service -> structured logs
                                              `-- Audit Service -> nexa_audit
```

Account Service owns balances and performs source debit, destination credit, two immutable ledger inserts, idempotency persistence, and outbox insertion in one PostgreSQL transaction. Accounts are locked in stable ID order to avoid deadlocks. A scheduled publisher sends committed outbox events to Kafka and marks them published only after broker acknowledgement.

Transaction Service is a read projection, not the balance authority. Its consumers use transaction/event IDs as primary keys, making Kafka redelivery idempotent. Notification Service initially writes structured logs. Audit Service stores actor, action, resource, status, time, and correlation ID without tokens, passwords, or other secrets.

The gateway contains no banking rules. It validates JWT issuer/signature, permits public auth endpoints, routes downstream requests, preserves bearer tokens, and propagates `X-Correlation-ID`. The MCP server exposes Streamable HTTP at `/mcp`, validates the same JWT independently, and calls only gateway REST APIs—never service databases.

The Agent Service derives identity and conversation scope from the verified JWT. Each chat request creates a short-lived MCP client carrying that caller's bearer token, discovers the banking tools, exposes them to Spring AI `ChatClient`, and closes the client after the model response. The model may select tools, but deterministic services retain authorization and money-movement authority. Customer-scoped chat memory supports ordinary follow-ups; transfer execution still requires the MCP confirmation token and cannot bypass `prepareTransfer`.

## Remaining phases

Live Ollama/MCP smoke testing, deterministic multi-turn transfer orchestration, RAG/pgvector, frontend, service container images, observability, and Kubernetes remain deferred.
