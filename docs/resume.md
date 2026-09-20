# Résumé and portfolio copy

Use only claims you can explain and demonstrate. Replace the bracketed link with the public deployment URL after an operator deploys the production overlay.

## One-line project entry

**Nexa Bank — AI-enabled distributed banking platform** | Java, Spring Boot, Spring AI, PostgreSQL/pgvector, Kafka, Redis, React, Docker, Kubernetes | [GitHub](https://github.com/25Rohit25/NexaBank) | [Live demo]

## Résumé bullets

- Built a backend-first banking platform across eight Spring Boot services with JWT-scoped authorization, immutable ledger entries, atomic transfers, durable idempotency, and a transactional Kafka outbox.
- Implemented a secured AI banking agent using MCP tools for live account operations and pgvector RAG for grounded policy answers, with explicit transfer confirmation and cross-customer isolation.
- Delivered a responsive React customer application plus Docker Compose and Kubernetes environments with health probes, autoscaling, network policies, external secrets, Prometheus, and Grafana.
- Established automated quality gates with Maven and Testcontainers integration tests, deterministic AI safety evaluations, frontend lint/build checks, container builds, CodeQL, dependency review, and zero-advisory npm auditing.

## Short portfolio description

Nexa Bank demonstrates how generative AI can be added to a financial system without giving the model authority over money or identity. The model selects an intent, while authenticated Spring Boot services validate ownership and execute deterministic operations. Live banking data flows through MCP tools; policy knowledge flows through evidence-gated RAG. PostgreSQL remains the system of record, Redis holds short-lived state, and Kafka distributes committed events to transaction, notification, and audit consumers.

## Interview talking points

1. **Transfer correctness:** account rows are locked in stable order; balances, ledger entries, replayable idempotency results, and the outbox event commit atomically.
2. **Event reliability:** the transactional outbox closes the database/Kafka dual-write gap, while stable event IDs make projections idempotent.
3. **AI safety:** customer identity comes from the JWT, not the prompt; write tools use two-step confirmation; policy responses require retrieved evidence.
4. **Service boundaries:** each stateful service owns its database, and downstream views consume events instead of reading another service's tables.
5. **Production engineering:** immutable images, probes, resource limits, autoscaling, network policy, external secrets, observability, CI, and security scanning are versioned with the application.

## Demo proof

Follow [demo.md](demo.md) for the seven-scenario recording. Keep the final video concise: architecture, traditional transfer, MCP lookup, guarded AI transfer, grounded policy answer, hybrid answer, isolation denial, and hallucination refusal.
